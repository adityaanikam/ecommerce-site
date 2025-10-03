# PowerShell script to replace all image URLs with simple placeholder URLs without query params
$files = Get-ChildItem -Path "src" -Filter "*.java" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    # Replace any existing via.placeholder.com URLs with simple ones
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff'
    # Replace any remaining picsum URLs
    $content = $content -replace 'https://picsum\.photos/seed/([^/]+)/800/800', 'https://via.placeholder.com/800x800/6366f1/ffffff'
    $content = $content -replace 'https://picsum\.photos/800/600', 'https://via.placeholder.com/800x600/6366f1/ffffff'
    Set-Content $file.FullName -Value $content -NoNewline
    Write-Host "Updated: $($file.FullName)"
}

Write-Host "Simple image URL replacement completed!"
