<#
.SYNOPSIS
  AgriMonitor - compile + run

.USAGE
  powershell -ExecutionPolicy Bypass -File .\Build-Run.ps1
  powershell -ExecutionPolicy Bypass -File .\Build-Run.ps1 -NoRun        # shudhu compile
  powershell -ExecutionPolicy Bypass -File .\Build-Run.ps1 -ProjectRoot "D:\DEV\AgriMonitor"
#>
param(
    [string]$ProjectRoot = (Get-Location).Path,
    [switch]$NoRun
)

$ErrorActionPreference = 'Stop'

if (-not (Test-Path (Join-Path $ProjectRoot 'src\main\java\com\agrimonitor'))) {
    if (Test-Path (Join-Path $PSScriptRoot 'src\main\java\com\agrimonitor')) { $ProjectRoot = $PSScriptRoot }
    else { throw "AgriMonitor project paoa jayni. -ProjectRoot diye path dao." }
}
Set-Location $ProjectRoot     # .env project root theke load hoy, tai ekhane theke run korte hobe

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw "javac paoa jayni. JDK install koro ar PATH / JAVA_HOME set koro (java -version & javac -version check koro)."
}
if (-not (Test-Path '.env')) {
    Write-Host "WARNING: .env nai - MONGODB_URI na thakle database connect hobe na." -ForegroundColor Yellow
}

# misplaced PasswordUtil (package com.agrimonitor.util) guard
if ((Test-Path 'src\main\java\util\PasswordUtil.java') -and -not (Test-Path 'src\main\java\com\agrimonitor\util\PasswordUtil.java')) {
    throw "PasswordUtil.java vul folder-e ache. Age Apply-DemoData.ps1 chalao."
}

if (Test-Path 'out') { Remove-Item 'out' -Recurse -Force }
New-Item -ItemType Directory -Path 'out' | Out-Null

$files = Get-ChildItem -Path 'src\main\java' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }
Write-Host "Compiling $($files.Count) files..." -ForegroundColor Cyan
& javac -cp 'libs/*' -d out -encoding UTF-8 @files
if ($LASTEXITCODE -ne 0) { throw "Compile failed (exit $LASTEXITCODE)" }
Write-Host "Compile OK" -ForegroundColor Green

if (-not $NoRun) {
    Write-Host "Starting AgriMonitor..." -ForegroundColor Cyan
    & java -cp 'out;libs/*' com.agrimonitor.Main
}
