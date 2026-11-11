$ErrorActionPreference = 'Stop'

$projectRoot = $PSScriptRoot
$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $javaHome = Split-Path (Split-Path (Get-Command java.exe -ErrorAction Stop).Source -Parent) -Parent
}
$maven = Join-Path $env:ProgramFiles 'JetBrains\IntelliJ IDEA 2026.2.2\plugins\maven-plugin\lib\maven3\bin\mvn.cmd'
if (-not (Test-Path $maven)) {
    throw "Maven was not found at '$maven'. Run clean javafx:jlink from IntelliJ's Maven panel, then rerun this script."
}

$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"
Push-Location $projectRoot
try {
    & $maven -DskipTests clean javafx:jlink
    if ($LASTEXITCODE -ne 0) { throw 'The JavaFX runtime image build failed.' }

    $image = Join-Path $projectRoot 'target\JavaJungle'
    $installer = Join-Path $projectRoot 'target\installer'
    $icon = Join-Path $projectRoot 'src\main\resources\org\javajungle\java-jungle-icon.ico'
    New-Item -ItemType Directory -Force -Path $installer | Out-Null

    & (Join-Path $javaHome 'bin\jpackage.exe') `
        --type exe `
        --app-image $image `
        --name JavaJungle `
        --app-version 1.0.0 `
        --dest $installer `
        --icon $icon `
        --win-menu `
        --win-shortcut
    if ($LASTEXITCODE -ne 0) { throw 'The Windows installer build failed. jpackage may require WiX Toolset on this computer.' }

    Write-Host "Installer created in: $installer"
} finally {
    Pop-Location
}
