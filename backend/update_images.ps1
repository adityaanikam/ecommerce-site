# PowerShell script to replace all image URLs with simple placeholder URLs
$files = Get-ChildItem -Path "src" -Filter "*.java" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace 'https://picsum\.photos/seed/([^/]+)/800/800', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://picsum\.photos/800/600', 'https://via.placeholder.com/800x600/6366f1/ffffff?text=Product'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    $content = $content -replace 'https://via\.placeholder\.com/800x800/6366f1/ffffff\?text=([^"]+)', 'https://via.placeholder.com/800x800/6366f1/ffffff?text=$1'
    Set-Content $file.FullName -Value $content -NoNewline
    Write-Host "Updated: $($file.FullName)"
}

Write-Host "Image URL replacement completed!"
