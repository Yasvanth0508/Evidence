# Evidence Backend API Automated Live Test Script for Windows PowerShell 5.1 / PowerShell 7
$baseUrl = "http://localhost:8080/api/v1"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  EVIDENCE BACKEND LIVE TEST SUITE       " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$passCount = 0
$failCount = 0

function Request-Api {
    param (
        [string]$Uri,
        [string]$Method = "GET",
        [string]$Body = $null,
        [hashtable]$Headers = @{}
    )

    $params = @{
        Uri = $Uri
        Method = $Method
        ContentType = "application/json"
        Headers = $Headers
    }
    if ($Body) {
        $params["Body"] = $Body
    }

    try {
        $response = Invoke-RestMethod @params
        return @{
            StatusCode = 200
            Data = $response
            Success = $true
        }
    } catch [System.Net.WebException] {
        $ex = $_.Exception
        $statusCode = [int]$ex.Response.StatusCode
        $reader = New-Object System.IO.StreamReader($ex.Response.GetResponseStream())
        $respBody = $reader.ReadToEnd()
        $json = $null
        try {
            $json = $respBody | ConvertFrom-Json
        } catch {}
        return @{
            StatusCode = $statusCode
            Data = $json
            Raw = $respBody
            Success = $false
        }
    } catch {
        return @{
            StatusCode = 500
            Data = $_.Exception.Message
            Success = $false
        }
    }
}

function Assert-Test {
    param (
        [string]$TestName,
        [bool]$Condition,
        [string]$Details = ""
    )
    if ($Condition) {
        Write-Host "[PASS] $TestName" -ForegroundColor Green
        if ($Details) { Write-Host "       $Details" -ForegroundColor DarkGray }
        $script:passCount++
    } else {
        Write-Host "[FAIL] $TestName" -ForegroundColor Red
        if ($Details) { Write-Host "       $Details" -ForegroundColor Yellow }
        $script:failCount++
    }
}

# 1. CANDIDATE MODULE TESTS
Write-Host "`n--- TESTING MODULE 2: CANDIDATE ENDPOINTS ---" -ForegroundColor Magenta

# 1.1 Search candidate
$searchRes = Request-Api -Uri "$baseUrl/candidates/search?email=rahul@example.com"
Assert-Test -TestName "1.1 Search candidate by email (rahul@example.com)" -Condition ($searchRes.StatusCode -eq 200 -and $searchRes.Data.success -eq $true) -Details "Candidate ID: $($searchRes.Data.data.id), Name: $($searchRes.Data.data.name)"
$candidateId = $searchRes.Data.data.id

# 1.2 Get candidate by ID
$getCanRes = Request-Api -Uri "$baseUrl/candidates/$candidateId"
Assert-Test -TestName "1.2 Get candidate by ID (GET /candidates/{id})" -Condition ($getCanRes.StatusCode -eq 200 -and $getCanRes.Data.data.id -eq $candidateId) -Details "Email: $($getCanRes.Data.data.email)"

# 1.3 Search candidate 404 (non-existent email)
$search404 = Request-Api -Uri "$baseUrl/candidates/search?email=notfound@example.com"
Assert-Test -TestName "1.3 Search candidate 404 not found" -Condition ($search404.StatusCode -eq 404 -and $search404.Data.errorCode -eq "CANDIDATE_NOT_FOUND") -Details "ErrorCode: $($search404.Data.errorCode)"

# 2. WORKSPACE MODULE TESTS
Write-Host "`n--- TESTING MODULE 1: WORKSPACE ENDPOINTS ---" -ForegroundColor Magenta

# 2.1 Create workspace
$wsBody = @{
    name = "Campus Hiring Drive 2026"
    description = "Automated assessment drive for Java Spring Boot engineers"
} | ConvertTo-Json
$createWsRes = Request-Api -Uri "$baseUrl/workspaces" -Method POST -Body $wsBody
Assert-Test -TestName "2.1 Create Workspace (POST /workspaces)" -Condition ($createWsRes.Data.success -eq $true -and $createWsRes.Data.data.id -ne $null) -Details "Workspace ID: $($createWsRes.Data.data.id), Status: $($createWsRes.Data.data.status)"
$workspaceId = $createWsRes.Data.data.id

# 2.2 List workspaces
$listWsRes = Request-Api -Uri "$baseUrl/workspaces"
Assert-Test -TestName "2.2 List Workspaces (GET /workspaces)" -Condition ($listWsRes.StatusCode -eq 200 -and $listWsRes.Data.data.Count -gt 0) -Details "Found $($listWsRes.Data.data.Count) workspace(s)"

# 2.3 Get workspace by ID
$getWsRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId"
Assert-Test -TestName "2.3 Get Workspace Details (GET /workspaces/{id})" -Condition ($getWsRes.StatusCode -eq 200 -and $getWsRes.Data.data.name -eq "Campus Hiring Drive 2026") -Details "Name: $($getWsRes.Data.data.name)"

# 2.4 Update workspace
$updateWsBody = @{
    name = "Campus Hiring Drive 2026 - Updated"
    description = "Updated description for automated tests"
} | ConvertTo-Json
$updateWsRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId" -Method PUT -Body $updateWsBody
Assert-Test -TestName "2.4 Update Workspace (PUT /workspaces/{id})" -Condition ($updateWsRes.StatusCode -eq 200 -and $updateWsRes.Data.data.name -eq "Campus Hiring Drive 2026 - Updated") -Details "Updated Name: $($updateWsRes.Data.data.name)"

# 2.5 Add candidate to workspace
$addCanBody = @{
    email = "rahul@example.com"
} | ConvertTo-Json
$addCanRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId/candidates" -Method POST -Body $addCanBody
Assert-Test -TestName "2.5 Add Candidate to Workspace (POST /workspaces/{id}/candidates)" -Condition ($addCanRes.Data.data.candidate.email -eq "rahul@example.com") -Details "Enrolled Candidate ID: $($addCanRes.Data.data.candidate.id)"

# 2.6 Duplicate candidate enrollment test (409 Conflict)
$dupCanRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId/candidates" -Method POST -Body $addCanBody
Assert-Test -TestName "2.6 Duplicate Candidate Enrollment 409 Conflict" -Condition ($dupCanRes.StatusCode -eq 409 -and $dupCanRes.Data.errorCode -eq "DUPLICATE_RESOURCE") -Details "ErrorCode: $($dupCanRes.Data.errorCode)"

# 2.7 List candidates in workspace
$listWsCanRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId/candidates"
Assert-Test -TestName "2.7 List Candidates in Workspace (GET /workspaces/{id}/candidates)" -Condition ($listWsCanRes.StatusCode -eq 200 -and $listWsCanRes.Data.data.Count -gt 0) -Details "Total candidates enrolled: $($listWsCanRes.Data.data.Count)"

# 2.8 Candidate's workspaces query
$canWsRes = Request-Api -Uri "$baseUrl/candidates/$candidateId/workspaces"
Assert-Test -TestName "2.8 List Workspaces for Candidate (GET /candidates/{id}/workspaces)" -Condition ($canWsRes.StatusCode -eq 200 -and $canWsRes.Data.data.Count -gt 0) -Details "Candidate enrolled in: $($canWsRes.Data.data[0].workspaceName)"

# 3. ASSESSMENT MODULE TESTS
Write-Host "`n--- TESTING MODULE 3: ASSESSMENT ENDPOINTS ---" -ForegroundColor Magenta

# 3.1 Create assessment
$startAt = (Get-Date).ToUniversalTime().AddMinutes(-10).ToString("yyyy-MM-ddTHH:mm:ssZ")
$endAt = (Get-Date).ToUniversalTime().AddHours(3).ToString("yyyy-MM-ddTHH:mm:ssZ")

$asmntBody = @{
    candidateId = $candidateId
    repositoryUrl = "https://github.com/candidate/spring-boot-notes-app"
    branchName = "main"
    backendRootDirectory = "backend"
    difficulty = "INTERMEDIATE"
    durationMinutes = 90
    scheduledStartAt = $startAt
    scheduledEndAt = $endAt
} | ConvertTo-Json
$createAsmntRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId/assessments" -Method POST -Body $asmntBody
Assert-Test -TestName "3.1 Create Assessment (POST /workspaces/{id}/assessments)" -Condition ($createAsmntRes.Data.success -eq $true -and $createAsmntRes.Data.data.assessmentId -ne $null) -Details "Assessment ID: $($createAsmntRes.Data.data.assessmentId), Status: $($createAsmntRes.Data.data.status)"
$assessmentId = $createAsmntRes.Data.data.assessmentId

# 3.2 List assessments in workspace
$listAsmntRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId/assessments"
Assert-Test -TestName "3.2 List Assessments in Workspace (GET /workspaces/{id}/assessments)" -Condition ($listAsmntRes.StatusCode -eq 200 -and $listAsmntRes.Data.data.Count -gt 0) -Details "Assessments in workspace: $($listAsmntRes.Data.data.Count)"

# 3.3 Get assessment details
$getAsmntRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId"
Assert-Test -TestName "3.3 Get Assessment Details (GET /assessments/{id})" -Condition ($getAsmntRes.StatusCode -eq 200 -and $getAsmntRes.Data.data.assessmentId -eq $assessmentId) -Details "Repo: $($getAsmntRes.Data.data.repositoryUrl), Candidate: $($getAsmntRes.Data.data.candidateName)"

# 3.4 Update assessment
$updateAsmntBody = @{
    difficulty = "DIFFICULT"
    durationMinutes = 120
} | ConvertTo-Json
$updateAsmntRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId" -Method PUT -Body $updateAsmntBody
Assert-Test -TestName "3.4 Update Assessment (PUT /assessments/{id})" -Condition ($updateAsmntRes.StatusCode -eq 200 -and $updateAsmntRes.Data.data.difficulty -eq "DIFFICULT") -Details "New Difficulty: $($updateAsmntRes.Data.data.difficulty), Duration: $($updateAsmntRes.Data.data.durationMinutes)"

# 3.5 Check AI processing status
$procRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/processing-status"
Assert-Test -TestName "3.5 Get Processing Status (GET /assessments/{id}/processing-status)" -Condition ($procRes.StatusCode -eq 200 -and $procRes.Data.data.stages.Count -eq 4) -Details "Stages: $(($procRes.Data.data.stages | ForEach-Object { $_.name }) -join ', ')"

# 3.6 Candidate starts assessment
$startRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/start" -Method POST
Assert-Test -TestName "3.6 Candidate Starts Assessment (POST /assessments/{id}/start)" -Condition ($startRes.StatusCode -eq 200 -and $startRes.Data.data.status -eq "IN_PROGRESS") -Details "Status: $($startRes.Data.data.status)"

# 3.7 Candidate submits assessment
$submitRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/submit" -Method POST
Assert-Test -TestName "3.7 Candidate Submits Assessment (POST /assessments/{id}/submit)" -Condition ($submitRes.StatusCode -eq 200 -and $submitRes.Data.data.status -eq "EVALUATING") -Details "Submission ID: $($submitRes.Data.data.submissionId)"

# 3.8 Check candidate's assessments list
$canAsmntRes = Request-Api -Uri "$baseUrl/candidates/$candidateId/assessments"
Assert-Test -TestName "3.8 Candidate Assessments Listing (GET /candidates/{id}/assessments)" -Condition ($canAsmntRes.StatusCode -eq 200 -and $canAsmntRes.Data.data.Count -gt 0) -Details "Candidate Assessment Score: $($canAsmntRes.Data.data[0].score)"

# 3.9 Create a secondary assessment to test cancel endpoint
$asmnt2Body = @{
    candidateId = $candidateId
    repositoryUrl = "https://github.com/candidate/spring-boot-notes-app"
    branchName = "main"
    backendRootDirectory = "backend"
    difficulty = "EASY"
    durationMinutes = 60
    scheduledStartAt = $startAt
    scheduledEndAt = $endAt
} | ConvertTo-Json
$createAsmnt2Res = Request-Api -Uri "$baseUrl/workspaces/$workspaceId/assessments" -Method POST -Body $asmnt2Body
$assessment2Id = $createAsmnt2Res.Data.data.assessmentId

# Cancel secondary assessment
$cancelRes = Request-Api -Uri "$baseUrl/assessments/$assessment2Id/cancel" -Method POST
Assert-Test -TestName "3.9 Cancel Assessment (POST /assessments/{id}/cancel)" -Condition ($cancelRes.StatusCode -eq 200 -and $cancelRes.Data.data.status -eq "CANCELLED") -Details "Cancelled Assessment Status: $($cancelRes.Data.data.status)"

# 4. CLEANUP / REMOVAL TESTS
Write-Host "`n--- TESTING REMOVAL & DELETION ENDPOINTS ---" -ForegroundColor Magenta

# 4.1 Remove candidate from workspace
$removeCanRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId/candidates/$candidateId" -Method DELETE
Assert-Test -TestName "4.1 Remove Candidate from Workspace (DELETE /workspaces/{id}/candidates/{canId})" -Condition ($removeCanRes.StatusCode -eq 200 -and $removeCanRes.Data.success -eq $true) -Details "Message: $($removeCanRes.Data.message)"

# 4.2 Delete workspace
$delWsRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId" -Method DELETE
Assert-Test -TestName "4.2 Delete Workspace (DELETE /workspaces/{id})" -Condition ($delWsRes.StatusCode -eq 200 -and $delWsRes.Data.success -eq $true) -Details "Message: $($delWsRes.Data.message)"

# SUMMARY
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  TEST RESULTS SUMMARY                   " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "TOTAL PASSED: $passCount" -ForegroundColor Green
Write-Host "TOTAL FAILED: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host "=========================================" -ForegroundColor Cyan
