$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$driverVersion = '9.3.0'
$driverDirectory = Join-Path $projectRoot 'target\dependencies'
$driverJar = Join-Path $driverDirectory "mysql-connector-j-$driverVersion.jar"
$bcryptVersion = '0.4'
$bcryptJar = Join-Path $driverDirectory "jbcrypt-$bcryptVersion.jar"
$classesDirectory = Join-Path $projectRoot 'target\classes'

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'JDK 17+ is required. Install a JDK and confirm javac -version works.'
}
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw 'Java is not on PATH. Install a JDK and confirm java -version works.'
}

if ([string]::IsNullOrWhiteSpace($env:FLIGHT_DB_USER)) {
    $env:FLIGHT_DB_USER = 'flight_app'
}
if ([string]::IsNullOrWhiteSpace($env:FLIGHT_DB_PASSWORD)) {
    $securePassword = Read-Host "Enter the MySQL password for $($env:FLIGHT_DB_USER)" -AsSecureString
    $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $env:FLIGHT_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
        $securePassword.Dispose()
    }
}

New-Item -ItemType Directory -Force -Path $driverDirectory,$classesDirectory | Out-Null
if (-not (Test-Path -LiteralPath $driverJar)) {
    $driverUrl = "https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/$driverVersion/mysql-connector-j-$driverVersion.jar"
    Write-Host 'Downloading MySQL Connector/J from Maven Central...'
    Invoke-WebRequest -Uri $driverUrl -OutFile $driverJar
}
if (-not (Test-Path -LiteralPath $bcryptJar)) {
    $bcryptUrl = "https://repo.maven.apache.org/maven2/org/mindrot/jbcrypt/$bcryptVersion/jbcrypt-$bcryptVersion.jar"
    Write-Host 'Downloading password compatibility library from Maven Central...'
    Invoke-WebRequest -Uri $bcryptUrl -OutFile $bcryptJar
}

$sources = @(Get-ChildItem (Join-Path $projectRoot 'src\main\java') -Recurse -Filter '*.java' | ForEach-Object FullName)
if ($sources.Count -eq 0) { throw 'No Java source files were found.' }
Write-Host 'Compiling the Swing application...'
& javac --release 17 -encoding UTF-8 -cp "$driverJar;$bcryptJar" -d $classesDirectory $sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host 'Starting Skyward Flight Booking...'
$classpath = "$classesDirectory;$driverJar;$bcryptJar"
& java -cp $classpath com.flightbooking.FlightBookingApp
exit $LASTEXITCODE
