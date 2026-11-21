package com.agrimonitor.service;

import com.agrimonitor.dao.CostDAO;
import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.dao.IrrigationDAO;
import com.agrimonitor.dao.PesticideDAO;
import com.agrimonitor.dao.SalesDAO;
import com.agrimonitor.model.Crop;
import com.agrimonitor.model.Field;
import com.agrimonitor.model.Irrigation;
import com.agrimonitor.model.Pesticide;
import com.agrimonitor.session.Session;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FarmContextBuilder {

    private static final long CACHE_MS = 60_000;
    private static final int MAX_FIELDS = 10, MAX_CROPS = 15, MAX_RECENT = 6;
    private static final DecimalFormat NUM = new DecimalFormat("#,##0.##");

    // simple cache so we do not hit the database + weather API on every single message
    private static String cachedFor;
    private static String cachedText;
    private static long cachedAt;

    private FarmContextBuilder() { }

    /** Forget the cached summary (call after the farmer adds/edits data, or on logout). */
    public static synchronized void invalidate() {
        cachedFor = null;
        cachedText = null;
    }

    /** Returns the summary, or "" if nothing could be read. Never throws. */
    public static synchronized String build() {
        String user = Session.getUserId();
        if (user == null) return "";
        long now = System.currentTimeMillis();
        if (user.equals(cachedFor) && cachedText != null && now - cachedAt < CACHE_MS) return cachedText;

        String text;
        try {
            text = collect();
        } catch (Throwable t) {
            System.out.println("FarmContextBuilder: could not read farm data: " + t.getMessage());
            text = "";
        }
        cachedFor = user;
        cachedText = text;
        cachedAt = now;
        return text;
    }

    private static String collect() {
        StringBuilder sb = new StringBuilder();
        sb.append("Farmer name: ").append(Session.getUsername()).append('\n');
        sb.append("Today: ").append(LocalDate.now()).append('\n');

        // ---- fields
        List<Field> fields = new ArrayList<>();
        Map<String, String> fieldNames = new HashMap<>();
        try {
            fields = new FieldDAO().getAllFields();
            for (Field f : fields) fieldNames.put(f.getFieldId(), f.getFieldName());
        } catch (Throwable ignored) { }
        sb.append("\nFIELDS (").append(fields.size()).append("):\n");
        if (fields.isEmpty()) sb.append("- none recorded yet\n");
        for (int i = 0; i < Math.min(MAX_FIELDS, fields.size()); i++) {
            Field f = fields.get(i);
            sb.append("- ").append(f.getFieldName()).append(" (id ").append(f.getFieldId()).append(")")
              .append(", location: ").append(f.getLocation())
              .append(", area: ").append(NUM.format(f.getAreaSize())).append('\n');
        }

        // ---- crops
        try {
            List<Crop> crops = new CropDAO().getAllCrops();
            sb.append("\nCROPS (").append(crops.size()).append("):\n");
            if (crops.isEmpty()) sb.append("- none recorded yet\n");
            for (int i = 0; i < Math.min(MAX_CROPS, crops.size()); i++) {
                Crop c = crops.get(i);
                String fn = fieldNames.get(c.getFieldId());
                sb.append("- ").append(c.getCropName()).append(" (").append(c.getCropType()).append(")")
                  .append(", growth duration: ").append(c.getGrowthDuration()).append(" days")
                  .append(", field: ").append(fn != null ? fn : c.getFieldId()).append('\n');
            }
        } catch (Throwable ignored) { }

        // ---- irrigation (most recent)
        try {
            List<Irrigation> list = new IrrigationDAO().getAllIrrigations();
            list.sort(Comparator.comparing((Irrigation x) -> String.valueOf(x.getIrrigationDate())).reversed());
            sb.append("\nRECENT IRRIGATION (water in litres):\n");
            if (list.isEmpty()) sb.append("- none recorded\n");
            for (int i = 0; i < Math.min(MAX_RECENT, list.size()); i++) {
                Irrigation x = list.get(i);
                sb.append("- ").append(x.getIrrigationDate()).append(": ")
                  .append(name(fieldNames, x.getFieldId())).append(", ")
                  .append(NUM.format(x.getWaterQuantity())).append(" L\n");
            }
        } catch (Throwable ignored) { }

        // ---- pesticides (most recent)
        try {
            List<Pesticide> list = new PesticideDAO().getAllPesticides();
            list.sort(Comparator.comparing((Pesticide x) -> String.valueOf(x.getApplicationDate())).reversed());
            sb.append("\nRECENT PESTICIDE USE:\n");
            if (list.isEmpty()) sb.append("- none recorded\n");
            for (int i = 0; i < Math.min(MAX_RECENT, list.size()); i++) {
                Pesticide x = list.get(i);
                sb.append("- ").append(x.getApplicationDate()).append(": ")
                  .append(x.getPesticideName()).append(", quantity ")
                  .append(NUM.format(x.getQuantity())).append(", ")
                  .append(name(fieldNames, x.getFieldId())).append('\n');
            }
        } catch (Throwable ignored) { }

        // ---- money (BDT)
        try {
            double cost = new CostDAO().getTotalCostAll();
            double sales = new SalesDAO().getTotalSalesAll();
            sb.append("\nFINANCE (BDT): total cost ").append(NUM.format(cost))
              .append(", total sales ").append(NUM.format(sales))
              .append(", ").append(sales - cost >= 0 ? "profit " : "loss ")
              .append(NUM.format(Math.abs(sales - cost))).append('\n');
            Map<String, Double> breakdown = new CostDAO().getCostBreakdownByCategory();
            if (!breakdown.isEmpty()) {
                sb.append("Cost breakdown: ");
                boolean first = true;
                for (Map.Entry<String, Double> e : breakdown.entrySet()) {
                    if (!first) sb.append("; ");
                    sb.append(e.getKey()).append(' ').append(NUM.format(e.getValue()));
                    first = false;
                }
                sb.append('\n');
            }
        } catch (Throwable ignored) { }

        // ---- weather at the first field's location (best effort, may fail offline)
        if (!fields.isEmpty() && fields.get(0).getLocation() != null && !fields.get(0).getLocation().trim().isEmpty()) {
            String city = fields.get(0).getLocation().trim();
            try {
                JSONObject w = new WeatherService().fetchRealWeather(city);
                JSONObject main = w.optJSONObject("main");
                String desc = w.optJSONArray("weather") != null && w.optJSONArray("weather").length() > 0
                        ? w.optJSONArray("weather").getJSONObject(0).optString("description", "") : "";
                sb.append("\nCURRENT WEATHER at ").append(city).append(": ");
                if (main != null) {
                    sb.append(NUM.format(main.optDouble("temp"))).append(" C, humidity ")
                      .append(main.optInt("humidity")).append("%, ");
                }
                sb.append(desc);
                JSONObject wind = w.optJSONObject("wind");
                if (wind != null) sb.append(", wind ").append(NUM.format(wind.optDouble("speed"))).append(" m/s");
                sb.append('\n');
            } catch (Throwable ignored) { }
        }
        return sb.toString();
    }

    private static String name(Map<String, String> names, String fieldId) {
        String n = names.get(fieldId);
        return n != null ? n : "field " + fieldId;
    }
}
