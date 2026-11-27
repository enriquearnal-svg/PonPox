param(
    [Parameter(Mandatory = $true)][string]$DatosPrivados,
    [Parameter(Mandatory = $true)][string]$Salida,
    [string]$InnoCompiler = "${env:ProgramFiles(x86)}\Inno Setup 6\ISCC.exe"
)

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$datos = (Resolve-Path $DatosPrivados).Path
$output = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Salida)
$jdk = $env:JAVA_HOME

function Invoke-Checked {
    param([string]$Program, [string[]]$Arguments)
    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Program falló con código $LASTEXITCODE." }
}

foreach ($path in @($datos, $output)) {
    if ($path.Equals($repo, [StringComparison]::OrdinalIgnoreCase) -or
        $path.StartsWith("$repo\", [StringComparison]::OrdinalIgnoreCase)) {
        throw "Los datos y el instalador deben quedar fuera del repositorio."
    }
}
if (-not $jdk -or -not (Test-Path "$jdk\bin\jpackage.exe") -or
    -not ((Get-Content "$jdk\release") -match '^JAVA_VERSION="25[."]') -or
    -not ((Get-Content "$jdk\release") -match '^OS_ARCH="(amd64|x86_64)"')) {
    throw 'JAVA_HOME debe apuntar a un JDK 25 para Windows x64.'
}
if (-not (Test-Path -LiteralPath $InnoCompiler -PathType Leaf)) {
    throw 'Se necesita Inno Setup 6.3 o posterior. Indica la ruta de ISCC.exe.'
}
foreach ($file in @('ponpox.db', 'instalacion-datos.txt', 'Informes', 'fotos trabajadores')) {
    if (-not (Test-Path -LiteralPath (Join-Path $datos $file))) {
        throw "Falta $file en la carpeta de datos preparada."
    }
}
if (Test-Path -LiteralPath (Join-Path $output 'PonPox_Setup.exe')) {
    throw 'Usa una carpeta de salida sin un instalador anterior.'
}

$work = Join-Path ([IO.Path]::GetTempPath()) ("PonPox-" + [Guid]::NewGuid())
try {
    $inputDir = Join-Path $work 'entrada'
    $classes = Join-Path $work 'clases'
    $tools = Join-Path $work 'recursos'
    New-Item -ItemType Directory -Force -Path $inputDir, $classes, $tools, $output | Out-Null
    $libs = @(Get-ChildItem -LiteralPath "$repo\lib" -Filter '*.jar' | Sort-Object Name)
    $sources = @(Get-ChildItem -LiteralPath "$repo\src" -Filter '*.java' | ForEach-Object FullName)
    $classpath = ($libs.FullName -join ';')
    Invoke-Checked "$jdk\bin\javac.exe" (@('-encoding', 'UTF-8', '-cp', $classpath, '-d', $classes) + $sources)
    $manifest = Join-Path $work 'MANIFEST.MF'
    @('Manifest-Version: 1.0', 'Main-Class: PonPox',
        ('Class-Path: ' + ($libs.Name -join ' ')), '') | Set-Content -Encoding Ascii $manifest
    Invoke-Checked "$jdk\bin\jar.exe" @('--create', '--file', "$inputDir\ponpox.jar", '--manifest', $manifest, '-C', $classes, '.')
    foreach ($lib in $libs) { Copy-Item -LiteralPath $lib.FullName -Destination $inputDir }
    Copy-Item -LiteralPath "$repo\plantillas" -Destination $inputDir -Recurse
    Copy-Item -LiteralPath "$repo\Logotipo PonPox.png" -Destination $inputDir
    Invoke-Checked "$jdk\bin\javac.exe" @('-encoding', 'UTF-8', '-cp', $classpath, '-d', $tools, "$PSScriptRoot\RecursosInstalador.java")
    Invoke-Checked "$jdk\bin\java.exe" @('-Djava.awt.headless=true', '-cp', "$tools;$classpath",
        'RecursosInstalador', "$repo\Logotipo PonPox.png", "$PSScriptRoot\manual.txt", $work)
    Invoke-Checked "$jdk\bin\jpackage.exe" @('--type', 'app-image', '--input', $inputDir, '--dest', $work,
        '--name', 'PonPox', '--app-version', '31.0.0', '--main-jar', 'ponpox.jar',
        '--main-class', 'PonPox', '--icon', "$work\PonPox.ico",
        '--add-modules', 'java.desktop,java.sql,java.xml,java.logging,java.naming,jdk.unsupported',
        '--java-options', '--enable-native-access=ALL-UNNAMED',
        '--java-options', '-Dponpox.instalador.windows=true')
    Invoke-Checked $InnoCompiler @("/DPackageDir=$work", "/DPrivateData=$datos",
        "/DOutputPath=$output", "$PSScriptRoot\PonPox.iss")
    $installer = Join-Path $output 'PonPox_Setup.exe'
    if (-not (Test-Path -LiteralPath $installer -PathType Leaf)) { throw 'No se generó el instalador.' }
    Get-FileHash -LiteralPath $installer -Algorithm SHA256
}
finally {
    if (Test-Path -LiteralPath $work) { Remove-Item -LiteralPath $work -Recurse -Force }
}
