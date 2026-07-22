$loginUrl = "http://localhost:8080/api/v1/auth/login"
$loginBody = @{
    phone = "+919876543210"
    password = "admin123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.token

Write-Host "Token: $token"

$headers = @{
    Authorization = "Bearer $token"
}

$dashboardUrl = "http://localhost:8080/api/v1/admin/reports/dashboard-summary"
$dashboardResponse = Invoke-RestMethod -Uri $dashboardUrl -Method Get -Headers $headers
Write-Host "Dashboard Summary:"
$dashboardResponse | ConvertTo-Json

$usersUrl = "http://localhost:8080/api/v1/admin/reports/users"
$usersResponse = Invoke-RestMethod -Uri $usersUrl -Method Get -Headers $headers
Write-Host "User Report:"
$usersResponse | ConvertTo-Json -Depth 3

$premiumUrl = "http://localhost:8080/api/v1/admin/reports/premium"
$premiumResponse = Invoke-RestMethod -Uri $premiumUrl -Method Get -Headers $headers
Write-Host "Premium Report:"
$premiumResponse | ConvertTo-Json
