param(
    [Parameter(Mandatory=$true)] [string] $id,
    [string] $output = "$PSScriptRoot\..\target\comprobante-$id.pdf",
    [string] $host = "http://localhost:8080",
    [string] $chromePath = "C:\Program Files\Google\Chrome\Application\chrome.exe"
)

# Ensure output directory exists
$dir = Split-Path $output -Parent
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

$url = "$host/facturacion/$id/print"

if (-not (Test-Path $chromePath)) {
    Write-Error "Chrome not found at $chromePath. Edit the script parameter `-chromePath` or install Chrome."
    exit 2
}

# Run headless chrome to print to PDF
& "$chromePath" --headless --disable-gpu --no-sandbox --print-to-pdf="$output" "$url"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Chrome exited with code $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Output "PDF generado: $output"
