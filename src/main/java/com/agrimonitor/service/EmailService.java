package com.agrimonitor.service;

import com.agrimonitor.db.EnvLoader;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

public class EmailService {

    private static final int TIMEOUT_MS = 15000;

    private BufferedReader in;
    private BufferedWriter out;

    public static void sendPasswordResetCode(String toEmail, String username, String code) throws Exception {
        String subject = "AgriMonitor password reset code";
        String body = "Hello " + username + ",\r\n\r\n"
                + "Your AgriMonitor password reset code is: " + code + "\r\n\r\n"
                + "This code is valid for 10 minutes. If you did not ask to reset your password, "
                + "you can ignore this email.\r\n\r\n"
                + "- AgriMonitor";
        new EmailService().send(toEmail, subject, body);
    }

    private static String setting(String key, String fallback) {
        String v = EnvLoader.get(key);
        if (v == null || v.trim().isEmpty()) {
            return fallback;
        }
        return v.trim();
    }

    private void send(String to, String subject, String body) throws Exception {
        if (!to.matches("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")) {
            throw new Exception("Invalid recipient email address.");
        }

        String user = setting("SMTP_EMAIL", null);
        String pass = setting("SMTP_PASSWORD", null);
        if (user == null || pass == null) {
            throw new Exception("Email service is not configured. Add SMTP_EMAIL and "
                    + "SMTP_PASSWORD to the .env file.");
        }
        pass = pass.replaceAll("\\s+", ""); // Gmail shows app passwords with spaces
        String host = setting("SMTP_HOST", "smtp.gmail.com");
        int port;
        try {
            port = Integer.parseInt(setting("SMTP_PORT", "465"));
        } catch (NumberFormatException e) {
            throw new Exception("SMTP_PORT in .env must be a number.");
        }

        Socket socket = null;
        try {
            socket = (port == 465) ? SSLSocketFactory.getDefault().createSocket() : new Socket();
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);
            if (socket instanceof SSLSocket) {
                verifyHostname((SSLSocket) socket);
                ((SSLSocket) socket).startHandshake();
            }
            open(socket);

            expect(220);
            command("EHLO agrimonitor.local", 250);

            if (port != 465) {
                command("STARTTLS", 220);
                SSLSocket ssl = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                        .createSocket(socket, host, port, true);
                verifyHostname(ssl);
                ssl.startHandshake();
                socket = ssl;
                open(socket);
                command("EHLO agrimonitor.local", 250);
            }

            command("AUTH LOGIN", 334);
            command(b64(user), 334);
            try {
                command(b64(pass), 235);
            } catch (IOException e) {
                throw new Exception("The mail server rejected the sender login. For Gmail, use a "
                        + "16-character App Password (2-Step Verification must be on), not the "
                        + "normal account password.");
            }

            command("MAIL FROM:<" + user + ">", 250);
            command("RCPT TO:<" + to + ">", 250, 251);
            command("DATA", 354);

            String message = "From: AgriMonitor <" + user + ">\r\n"
                    + "To: <" + to + ">\r\n"
                    + "Subject: =?UTF-8?B?" + b64(subject) + "?=\r\n"
                    + "Date: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now()) + "\r\n"
                    + "Message-ID: <" + UUID.randomUUID() + "@agrimonitor.local>\r\n"
                    + "MIME-Version: 1.0\r\n"
                    + "Content-Type: text/plain; charset=UTF-8\r\n"
                    + "Content-Transfer-Encoding: base64\r\n"
                    + "\r\n"
                    + Base64.getMimeEncoder(76, new byte[]{'\r', '\n'})
                            .encodeToString(body.getBytes(StandardCharsets.UTF_8))
                    + "\r\n.";
            command(message, 250);

            try {
                command("QUIT", 221);
            } catch (IOException ignored) {
                // message is already accepted
            }
        } catch (java.net.UnknownHostException e) {
            throw new Exception("Cannot find mail server '" + host + "'. Check your internet connection.");
        } catch (java.net.SocketTimeoutException e) {
            throw new Exception("Mail server did not respond in time. Check your internet connection.");
        } catch (java.net.ConnectException e) {
            throw new Exception("Cannot connect to the mail server. Check internet/firewall.");
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void verifyHostname(SSLSocket s) {
        SSLParameters p = s.getSSLParameters();
        p.setEndpointIdentificationAlgorithm("HTTPS");
        s.setSSLParameters(p);
    }

    private void open(Socket socket) throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    private void command(String line, int... okCodes) throws IOException {
        out.write(line);
        out.write("\r\n");
        out.flush();
        expect(okCodes);
    }

    private void expect(int... okCodes) throws IOException {
        String line;
        int code = -1;
        StringBuilder all = new StringBuilder();
        while ((line = in.readLine()) != null) {
            all.append(line).append(' ');
            if (line.length() >= 3) {
                try {
                    code = Integer.parseInt(line.substring(0, 3));
                } catch (NumberFormatException e) {
                    code = -1;
                }
            }
            // "250-..." means more lines follow, "250 ..." is the last line
            if (line.length() < 4 || line.charAt(3) != '-') {
                break;
            }
        }
        if (line == null && code == -1) {
            throw new IOException("Mail server closed the connection.");
        }
        for (int c : okCodes) {
            if (c == code) {
                return;
            }
        }
        throw new IOException("Mail server replied: " + all.toString().trim());
    }

    private static String b64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }
}
