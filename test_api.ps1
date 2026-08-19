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
        $statusCode = 500
        $json = $null
        $respBody = ""
        if ($ex.Response -ne $null) {
            $statusCode = [int]$ex.Response.StatusCode
            $reader = New-Object System.IO.StreamReader($ex.Response.GetResponseStream())
            $respBody = $reader.ReadToEnd()
            try {
                $json = $respBody | ConvertFrom-Json
            } catch {}
        }
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

# Wait for server to become ready
Write-Host "Checking backend availability..." -ForegroundColor Gray
$ready = $false
for ($i = 0; $i -lt 15; $i++) {
    $check = Request-Api -Uri "$baseUrl/workspaces"
    if ($check.StatusCode -eq 200) {
        $ready = $true
        Write-Host "Backend is READY!" -ForegroundColor Green
        break
    }
    Start-Sleep -Seconds 1
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

# 4. REPOSITORY ANALYSIS MODULE TESTS (MODULE 4)
Write-Host "`n--- TESTING MODULE 4: REPOSITORY ANALYSIS ENDPOINTS ---" -ForegroundColor Magenta

# 4.1 Get repository analysis
$repoAnalysisRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/repository-analysis"
Assert-Test -TestName "4.1 Get Repository Analysis (GET /assessments/{id}/repository-analysis)" -Condition ($repoAnalysisRes.StatusCode -eq 200 -and $repoAnalysisRes.Data.data.analysisStatus -eq "COMPLETED" -and $repoAnalysisRes.Data.data.sourceCodeStructure.controllers.Count -gt 0) -Details "Controllers found: $($repoAnalysisRes.Data.data.sourceCodeStructure.controllers -join ', '), Endpoints: $($repoAnalysisRes.Data.data.contentDetails.endpoints.Count)"

# 4.2 Get repository analysis status
$repoAnalysisStatusRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/repository-analysis/status"
Assert-Test -TestName "4.2 Get Repository Analysis Status (GET /assessments/{id}/repository-analysis/status)" -Condition ($repoAnalysisStatusRes.StatusCode -eq 200 -and $repoAnalysisStatusRes.Data.data.status -eq "COMPLETED") -Details "Status: $($repoAnalysisStatusRes.Data.data.status)"

# 5. FEATURE SPECIFICATION MODULE TESTS (MODULE 5)
Write-Host "`n--- TESTING MODULE 5: FEATURE SPECIFICATION ENDPOINTS ---" -ForegroundColor Magenta

# 5.1 Get feature specification
$featureRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/feature"
Assert-Test -TestName "5.1 Get Feature Specification (GET /assessments/{id}/feature)" -Condition ($featureRes.StatusCode -eq 200 -and $featureRes.Data.data.endpoint -eq "/api/notes/search" -and $featureRes.Data.data.requirements.Count -gt 0) -Details "Title: $($featureRes.Data.data.title), Target Endpoint: $($featureRes.Data.data.httpMethod) $($featureRes.Data.data.endpoint)"

# 6. FILE EXPLORER MODULE TESTS (MODULE 6)
Write-Host "`n--- TESTING MODULE 6: FILE EXPLORER ENDPOINTS ---" -ForegroundColor Magenta

# 6.1 Get file tree
$fileTreeRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/files"
Assert-Test -TestName "6.1 Get File Tree (GET /assessments/{id}/files)" -Condition ($fileTreeRes.StatusCode -eq 200 -and $fileTreeRes.Data.data.Count -gt 0) -Details "Root items in file tree: $(($fileTreeRes.Data.data | ForEach-Object { $_.name }) -join ', ')"

# 6.2 Get file content
$fileContentRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/files/content?path=src/main/java/com/example/demo/controller/NoteController.java"
Assert-Test -TestName "6.2 Get File Content (GET /assessments/{id}/files/content)" -Condition ($fileContentRes.StatusCode -eq 200 -and $fileContentRes.Data.data.content -match "NoteController") -Details "Retrieved file: $($fileContentRes.Data.data.path), Length: $($fileContentRes.Data.data.content.Length) chars"

# 6.3 Save file content
$saveFileBody = @{
    path = "src/main/java/com/example/demo/controller/NoteController.java"
    content = "// updated note controller content"
} | ConvertTo-Json
$saveFileRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/files/content" -Method PUT -Body $saveFileBody
Assert-Test -TestName "6.3 Save File Content (PUT /assessments/{id}/files/content)" -Condition ($saveFileRes.StatusCode -eq 200 -and $saveFileRes.Data.success -eq $true) -Details "Saved Path: $($saveFileRes.Data.data.path), SavedAt: $($saveFileRes.Data.data.savedAt)"

# 7. APPLICATION EXECUTION MODULE TESTS (MODULE 7)
Write-Host "`n--- TESTING MODULE 7: EXECUTION ENDPOINTS ---" -ForegroundColor Magenta

# 7.1 Run application
$runRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/run" -Method POST
Assert-Test -TestName "7.1 Run Application (POST /assessments/{id}/run)" -Condition ($runRes.StatusCode -eq 200 -and $runRes.Data.data.containerStatus -eq "RUNNING") -Details "Container Status: $($runRes.Data.data.containerStatus), App Status: $($runRes.Data.data.applicationStatus)"

# 7.2 Get execution status
$execStatusRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/execution/status"
Assert-Test -TestName "7.2 Get Execution Status (GET /assessments/{id}/execution/status)" -Condition ($execStatusRes.StatusCode -eq 200 -and $execStatusRes.Data.data.buildStatus -eq "SUCCESS") -Details "Build Status: $($execStatusRes.Data.data.buildStatus)"

# 7.3 Get execution logs
$execLogsRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/execution/logs"
Assert-Test -TestName "7.3 Get Execution Logs (GET /assessments/{id}/execution/logs)" -Condition ($execLogsRes.StatusCode -eq 200 -and $execLogsRes.Data.data.logs -match "Spring Boot") -Details "Logs Length: $($execLogsRes.Data.data.logs.Length) characters"

# 7.4 Stop application
$stopRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/stop" -Method POST
Assert-Test -TestName "7.4 Stop Application (POST /assessments/{id}/stop)" -Condition ($stopRes.StatusCode -eq 200 -and $stopRes.Data.data.containerStatus -eq "STOPPED") -Details "Container Status: $($stopRes.Data.data.containerStatus)"

# 8. EVALUATION & RESULTS MODULE TESTS (MODULE 8)
Write-Host "`n--- TESTING MODULE 8: EVALUATION & RESULTS ENDPOINTS ---" -ForegroundColor Magenta

# 8.1 Get candidate result
$candidateResultRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/result"
Assert-Test -TestName "8.1 Get Candidate Result (GET /assessments/{id}/result)" -Condition ($candidateResultRes.StatusCode -eq 200 -and $candidateResultRes.Data.data.score -ne $null) -Details "Score: $($candidateResultRes.Data.data.score), Passed: $($candidateResultRes.Data.data.testsPassed)/$($candidateResultRes.Data.data.totalTests)"

# 8.2 Get recruiter assessment report
$reportDetailRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/report"
Assert-Test -TestName "8.2 Get Assessment Report (GET /assessments/{id}/report)" -Condition ($reportDetailRes.StatusCode -eq 200 -and $reportDetailRes.Data.data.candidateEmail -eq "rahul@example.com") -Details "Candidate: $($reportDetailRes.Data.data.candidateName), Score: $($reportDetailRes.Data.data.score)"

# 8.3 Get test results breakdown
$testResultsRes = Request-Api -Uri "$baseUrl/assessments/$assessmentId/test-results"
Assert-Test -TestName "8.3 Get Test Results Breakdown (GET /assessments/{id}/test-results)" -Condition ($testResultsRes.StatusCode -eq 200 -and $testResultsRes.Data.data.results.Count -eq 5) -Details "Total: $($testResultsRes.Data.data.totalTests), Passed: $($testResultsRes.Data.data.passedTests), Failed: $($testResultsRes.Data.data.failedTests)"

# 9. DASHBOARD MODULE TESTS (MODULE 9)
Write-Host "`n--- TESTING MODULE 9: DASHBOARD ENDPOINTS ---" -ForegroundColor Magenta

# 9.1 Recruiter Dashboard
$recruiterDashRes = Request-Api -Uri "$baseUrl/recruiter/dashboard"
Assert-Test -TestName "9.1 Recruiter Dashboard (GET /recruiter/dashboard)" -Condition ($recruiterDashRes.StatusCode -eq 200 -and $recruiterDashRes.Data.data.workspaceCount -ge 1) -Details "Workspaces: $($recruiterDashRes.Data.data.workspaceCount), Candidates: $($recruiterDashRes.Data.data.candidateCount), Assessments: $($recruiterDashRes.Data.data.assessmentCount)"

# 9.2 Candidate Dashboard
$candidateDashRes = Request-Api -Uri "$baseUrl/candidate/dashboard"
Assert-Test -TestName "9.2 Candidate Dashboard (GET /candidate/dashboard)" -Condition ($candidateDashRes.StatusCode -eq 200) -Details "Scheduled: $($candidateDashRes.Data.data.scheduledAssessments.Count), Completed: $($candidateDashRes.Data.data.completedAssessments.Count)"

# 10. REPORTS & SELECTED CANDIDATES MODULE TESTS (MODULE 10)
Write-Host "`n--- TESTING MODULE 10: REPORTS & SELECTED CANDIDATES ENDPOINTS ---" -ForegroundColor Magenta

# 10.1 Reports List
$reportsListRes = Request-Api -Uri "$baseUrl/reports?workspaceId=$workspaceId"
Assert-Test -TestName "10.1 List Reports (GET /reports?workspaceId={id})" -Condition ($reportsListRes.StatusCode -eq 200 -and $reportsListRes.Data.data.reports.Count -gt 0) -Details "Total Reports Found: $($reportsListRes.Data.data.totalCount)"

# 10.2 Reports Summary
$reportsSummaryRes = Request-Api -Uri "$baseUrl/reports/summary"
Assert-Test -TestName "10.2 Reports Summary (GET /reports/summary)" -Condition ($reportsSummaryRes.StatusCode -eq 200) -Details "Total Completed: $($reportsSummaryRes.Data.data.totalCompleted), Pass Rate: $($reportsSummaryRes.Data.data.passRate)%"

# 10.3 Report by ID
$reportByIdRes = Request-Api -Uri "$baseUrl/reports/$assessmentId"
Assert-Test -TestName "10.3 Report by ID (GET /reports/{id})" -Condition ($reportByIdRes.StatusCode -eq 200 -and $reportByIdRes.Data.data.assessmentId -eq $assessmentId) -Details "Candidate: $($reportByIdRes.Data.data.candidateName)"

# 10.4 Select Candidate
$selectCanBody = @{
    candidateId = $candidateId
    workspaceId = $workspaceId
    assessmentId = $assessmentId
    notes = "Exceptional performance in Spring Boot search endpoint implementation"
} | ConvertTo-Json
$selectCanRes = Request-Api -Uri "$baseUrl/selected-candidates" -Method POST -Body $selectCanBody
Assert-Test -TestName "10.4 Select Candidate (POST /selected-candidates)" -Condition ($selectCanRes.Data.success -eq $true -and $selectCanRes.Data.data.candidateId -eq $candidateId) -Details "Selected Candidate: $($selectCanRes.Data.data.candidateName), Notes: $($selectCanRes.Data.data.selectionNotes)"
$selectedId = $selectCanRes.Data.data.id

# 10.5 Duplicate Selection 409 Conflict
$dupSelectRes = Request-Api -Uri "$baseUrl/selected-candidates" -Method POST -Body $selectCanBody
Assert-Test -TestName "10.5 Duplicate Selection 409 Conflict" -Condition ($dupSelectRes.StatusCode -eq 409 -and $dupSelectRes.Data.errorCode -eq "DUPLICATE_SELECTION") -Details "ErrorCode: $($dupSelectRes.Data.errorCode)"

# 10.6 List Selected Candidates
$listSelectedRes = Request-Api -Uri "$baseUrl/selected-candidates?workspaceId=$workspaceId"
Assert-Test -TestName "10.6 List Selected Candidates (GET /selected-candidates?workspaceId={id})" -Condition ($listSelectedRes.StatusCode -eq 200 -and $listSelectedRes.Data.data.Count -gt 0) -Details "Total Selected Candidates: $($listSelectedRes.Data.data.Count)"

# 10.7 Delete Selected Candidate
$delSelectedRes = Request-Api -Uri "$baseUrl/selected-candidates/$selectedId" -Method DELETE
Assert-Test -TestName "10.7 Remove Selected Candidate (DELETE /selected-candidates/{id})" -Condition ($delSelectedRes.StatusCode -eq 200 -and $delSelectedRes.Data.success -eq $true) -Details "Message: $($delSelectedRes.Data.message)"

# 11. CLEANUP / REMOVAL TESTS
Write-Host "`n--- TESTING REMOVAL & DELETION ENDPOINTS ---" -ForegroundColor Magenta

# 11.1 Remove candidate from workspace
$removeCanRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId/candidates/$candidateId" -Method DELETE
Assert-Test -TestName "11.1 Remove Candidate from Workspace (DELETE /workspaces/{id}/candidates/{canId})" -Condition ($removeCanRes.StatusCode -eq 200 -and $removeCanRes.Data.success -eq $true) -Details "Message: $($removeCanRes.Data.message)"

# 11.2 Delete workspace
$delWsRes = Request-Api -Uri "$baseUrl/workspaces/$workspaceId" -Method DELETE
Assert-Test -TestName "11.2 Delete Workspace (DELETE /workspaces/{id})" -Condition ($delWsRes.StatusCode -eq 200 -and $delWsRes.Data.success -eq $true) -Details "Message: $($delWsRes.Data.message)"

# SUMMARY
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  TEST RESULTS SUMMARY                   " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "TOTAL PASSED: $passCount" -ForegroundColor Green
Write-Host "TOTAL FAILED: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host "=========================================" -ForegroundColor Cyan
