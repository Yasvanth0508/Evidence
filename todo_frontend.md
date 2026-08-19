# Evidence Frontend — UI Analysis & Implementation Roadmap

> **Context:** This document provides a comprehensive frontend implementation plan based on the UI design mockups for the EVIDENCE platform. It defines the design system, folder structure, and step-by-step phases to build a highly optimized, pure React + TypeScript SPA.
> **Current Strategy (Mock-First):** The frontend will initially be built completely disconnected from the backend. All UI components will be powered by local mock data files matching the exact schemas defined in the REST API Specification. Real backend integration will happen in a final swap phase.

---

## 1. Design System & Theming Analysis

Based on the uploaded UI mockups, the EVIDENCE platform uses a clean, modern, data-dense, and highly accessible B2B SaaS design language.

### Color Palette
- **Primary Brand (Evidence Orange):** `hsl(14, 90%, 55%)` or approx `#F05323`. Used for logos, primary CTA buttons (Get Started, Login, Download), and active states.
- **Backgrounds:** 
  - Main App Background: Very light gray/off-white (`#F9FAFB` or `zinc-50`).
  - Surface/Card Background: Pure White (`#FFFFFF`) with subtle shadows (`shadow-sm`).
- **Text:** 
  - High Contrast (Headings/Primary): Dark Slate (`#111827` or `gray-900`).
  - Muted (Labels/Secondary): Medium Gray (`#6B7280` or `gray-500`).
- **Status & Semantic Accents:**
  - **Success (Green):** `#10B981` (Completed status, Strengths, Tests Passed).
  - **Error/Fail (Red):** `#EF4444` (Bugs, Failed tests, Syntax errors).
  - **AI/Magic (Purple):** `#8B5CF6` (AI-Generated Summary, Total Assessments).
  - **Information (Blue):** `#3B82F6` (Data Flow bugs, generic info).
  - **Warning (Yellow/Amber):** `#F59E0B` (Medium Risk Badges, Top Performer Ranks).

### Typography
- **Font Family:** **Inter** (via Google Fonts). 
- **Weights:** Heavy use of Medium (500) and SemiBold (600) for headers and table headers. Regular (400) for body copy.

---

## 2. Tech Stack & Performance Strategy

- **Core Framework:** React 18 + TypeScript.
- **Build Tool:** Vite.
- **Routing:** `react-router-dom` v6 (with lazy loading).
- **Data Fetching:** `@tanstack/react-query` (Configured to fetch from our local mock services with artificial delays to test loading states).
- **State Management:** `zustand`.
- **Styling:** Tailwind CSS + `shadcn/ui`.
- **Code Editor:** `@monaco-editor/react`.

---

## 3. Frontend Folder Structure (Mock-First Architecture)

```text
frontend/
├── public/                     # Static assets (images, favicon)
├── index.html                  
├── vite.config.ts              
├── src/
│   ├── main.tsx                
│   ├── App.tsx                 
│   │
│   ├── routes/                 # Route definitions and Lazy loading wrappers
│   │
│   ├── mocks/                  # 🛑 LOCAL MOCK DATA LAYER 🛑
│   │   ├── data/               # Static mock objects based on API Spec
│   │   │   ├── auth.mock.ts
│   │   │   ├── dashboard.mock.ts
│   │   │   ├── candidates.mock.ts
│   │   │   └── assessment.mock.ts
│   │   └── services/           # Simulated API calls (with setTimeout delays)
│   │       ├── mockAuthService.ts
│   │       ├── mockDashboardService.ts
│   │       └── mockAssessmentService.ts
│   │
│   ├── pages/                  # Page-level components
│   │   ├── public/             # Marketing & Landing
│   │   ├── auth/               # Authentication
│   │   ├── dashboard/          # Recruiter Dashboard
│   │   └── assessment/         # Candidate IDE
│   │
│   ├── components/
│   │   ├── ui/                 # Reusable shadcn components
│   │   ├── layout/             # Shared layouts
│   │   ├── dashboard/          # Dashboard specific components
│   │   ├── candidate/          # Candidate report components
│   │   └── ide/                # Heavy IDE components
│   │
│   ├── lib/                    
│   │   ├── apiClient.ts        # Base client (currently routes to mock services)
│   │   └── utils.ts            
│   │
│   ├── hooks/                  # React Query hooks consuming the API client
│   │   ├── useAuth.ts          
│   │   └── api/                
│   │       ├── useDashboard.ts
│   │       └── useAssessments.ts
│   │
│   ├── types/                  # TypeScript interfaces matching API DTOs
│   └── styles/                 # Global CSS
```

---

## 4. Step-by-Step Implementation Roadmap

### Phase 1: Setup & Design System (Sprint 1) - ✅ COMPLETED
1. [x] Initialize Vite React-TS project.
2. [x] Install `react-router-dom`, `@tanstack/react-query`, `zustand`, `lucide-react`, `recharts`, `clsx`, `tailwind-merge`, `class-variance-authority`.
3. [x] Configure Tailwind CSS with Evidence brand colors, custom font (`Inter`), and initialize `shadcn/ui` primitives (Button, Card, Badge, Input, Table, Tabs, Logo).

### Phase 2: Implement Local Mock Data Layer (Sprint 1) - ✅ COMPLETED
1. [x] **Define Types:** Translated JSON schemas from `Evidence_REST_API_Specification_AI.md` into TypeScript interfaces in `src/types/index.ts`.
2. [x] **Create Mock Objects:** Created mock datasets in `src/mocks/data/` (`dashboard.mock.ts`, `candidates.mock.ts`, `assessment.mock.ts`) matching UI mockups and API specs.
3. [x] **Simulate API Delays:** Created simulated asynchronous services in `src/mocks/services/` (`mockDashboardService.ts`, `mockCandidateService.ts`, `mockAssessmentService.ts`, `mockAuthService.ts`) with realistic latency and React Query hooks in `src/hooks/`.

### Phase 3: Landing Page & Auth Flow (Sprint 2) - ✅ COMPLETED
1. [x] **Landing Page (`/`):** Built public Navbar, Hero with IDE mockup preview, Core Evaluation Taxonomy cards, 4-step How-It-Works timeline, CTA banner, and footer matching mockup.
2. [x] **Auth Pages (`/login`, `/signup`):** 
   - [x] Built Login form with Email, Password (with eye toggle), Remember Me, Google/GitHub/Microsoft OAuth buttons, and security badges.
   - [x] Built Signup form with Full Name, Email, Organization, Role selector (Recruiter vs Candidate), Password/Confirm Password.
   - [x] Connected both to `mockAuthService.ts` and Zustand `useAuthStore` to persist mock sessions and redirect to `/dashboard`.

### Phase 4: Recruiter Dashboard UI (Sprint 2) - ✅ COMPLETED
1. [x] **Dashboard Shell:** Implemented `Sidebar` with full navigation & bottom recruiter card, `Topbar` with date range, filters, export, and user profile avatar, and `DashboardLayout` with route guard protection.
2. [x] **Dashboard Overview (`/dashboard`):**
   - [x] Connected to `useRecruiterDashboard()` React Query hook powered by `mockDashboardService`.
   - [x] Built 4 metric `StatCard` components with sparklines for Total Candidates, Total Assessments, Completion Rate, and Avg. Fix Time.
   - [x] Implemented Recharts `AssessmentDonutChart` (Completed, In Progress, Not Started breakdown) and `BugCategoriesBarChart` (Business Logic, Syntax, Data Flow failure rates).
   - [x] Built `RecentAssessmentsTable` with integrity badges and view action links, and `TopPerformersList` leaderboard.
   - [x] Built bottom `DashboardFilterBar` for search, tech stack, and status filtering.

### Phase 5: Candidate Reporting View (Sprint 3) - ✅ COMPLETED
1. [x] **Candidate Report (`/dashboard/candidates/:id`):**
   - [x] Connected to `useCandidateReport(id)` mock service via React Query.
   - [x] Implemented animated SVG `ScoreProgressCircle` (overall score radial gauge) and 3-tier metric blocks (Bugs Fixed, Test Cases Passed, Time Taken).
   - [x] Built `BugBreakdownTable` categorizing Data Flow, Syntax, and Business Logic bugs with status badges and score totals.
   - [x] Built `AiSummaryCard` with AI badge and purple container styling.
   - [x] Built `StrengthsImprovements` dual card layout with positive and constructive AI feedback.
   - [x] Built `IntegrityBehaviorTabs` supporting tabbed telemetry audit logs and risk analysis box.
   - [x] Added action buttons for Share with Team and Download PDF Report.

### Phase 6: Candidate IDE Interface (Sprint 3) - ✅ COMPLETED
1. [x] **IDE Workspace (`/assessment/:id`):**
   - [x] Installed and integrated `@monaco-editor/react` with VS Code light theme and Java syntax support.
   - [x] Built recursive `FileExplorer` with folder collapse/expand and active file switching.
   - [x] Built `TerminalConsole` with Terminal and Build Output tabs, build status indicators, and Spring Boot compilation logs.
   - [x] Built `FeatureSpecDrawer` displaying AI-generated task requirements, HTTP contracts, and constraints.
   - [x] Built `IdeHeader` with live countdown timer, "Run Build" execution simulation, and "Submit Assessment" modal.

### Phase 7: Final Backend Integration Swap (Sprint 4)
*Once the backend is fully complete and ready:*
1. **Update `apiClient.ts`:** Swap out the imports from `src/mocks/services/*` with real `axios` calls pointing to `http://localhost:8080/api/v1`.
2. **Attach JWT:** Configure Axios interceptors to attach the Zustand auth token to all requests.
3. **Remove Delays:** The real network will now handle the latency, and React Query will process the real data seamlessly since the TypeScript schemas remain identical.
