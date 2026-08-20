import { Link } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import {
  ArrowRight,
  CheckCircle2,
  Terminal,
  Cpu,
  Layers,
  Sparkles,
  ShieldCheck,
  FolderGit2,
  Code2,
  CheckCheck,
  Users,
  Briefcase,
  PlayCircle,
  FileCheck2,
  Clock,
} from "lucide-react";
import {
  GithubIcon,
  LinkedinIcon,
  TwitterIcon,
  YoutubeIcon,
} from "@/components/ui/social-icons";

export const LandingPage = () => {
  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col font-sans">
      {/* ---------------------------------------------------- */}
      {/* 1. Public Top Navigation                             */}
      {/* ---------------------------------------------------- */}
      <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-gray-100 shadow-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <Logo size="md" />

          {/* Clean Navigation Links */}
          <nav className="hidden md:flex items-center space-x-8 text-sm font-medium text-gray-600">
            <a href="#overview" className="hover:text-gray-900 transition-colors">
              Platform Overview
            </a>
            <a href="#how-it-works" className="hover:text-gray-900 transition-colors">
              How It Works
            </a>
            <a href="#architecture" className="hover:text-gray-900 transition-colors">
              Key Capabilities
            </a>
            <a href="#portals" className="hover:text-gray-900 transition-colors">
              Role Portals
            </a>
          </nav>

          {/* Action Buttons for Recruiter and Candidate Portals */}
          <div className="flex items-center space-x-3">
            <Link to="/login?role=candidate">
              <Button
                variant="outline"
                size="sm"
                className="hidden sm:inline-flex font-semibold text-gray-700 hover:text-gray-900 border-gray-200 hover:bg-gray-50"
              >
                <Code2 className="w-3.5 h-3.5 mr-1.5 text-gray-500" />
                Candidate Portal
              </Button>
            </Link>
            <Link to="/login?role=recruiter">
              <Button
                variant="outline"
                size="sm"
                className="font-semibold text-gray-700 hover:text-gray-900 border-gray-200 hover:bg-gray-50"
              >
                <Briefcase className="w-3.5 h-3.5 mr-1.5 text-[#F05323]" />
                Recruiter Sign In
              </Button>
            </Link>
            <Link to="/signup">
              <Button size="sm" className="font-semibold shadow-xs bg-[#F05323] hover:bg-[#d94417]">
                Get Started
              </Button>
            </Link>
          </div>
        </div>
      </header>

      {/* ---------------------------------------------------- */}
      {/* 2. Hero Section with Live Spring Boot IDE Preview    */}
      {/* ---------------------------------------------------- */}
      <main className="flex-1">
        <section id="overview" className="py-16 sm:py-24 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            {/* Hero Left Content */}
            <div className="lg:col-span-6 space-y-6">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-orange-50 text-[#F05323] border border-orange-200/60 shadow-2xs">
                <span className="w-2 h-2 rounded-full bg-[#F05323] animate-pulse"></span>
                Java Spring Boot • Real Repositories • Black-Box Verification
              </div>

              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-gray-900 tracking-tight leading-[1.1]">
                Verify <br />
                <span className="text-[#F05323]">Real-World Code</span> <br />
                on Actual Projects
              </h1>

              <p className="text-base sm:text-lg text-gray-600 max-w-xl leading-relaxed">
                Evidence automatically analyzes candidate Java Spring Boot GitHub repositories and generates practical, project-specific feature tasks. Candidates build and run code in isolated Docker environments while hidden HTTP tests evaluate observable behavior.
              </p>

              {/* Primary Dual Role Action Buttons */}
              <div className="flex flex-wrap items-center gap-4 pt-2">
                <Link to="/login?role=recruiter">
                  <Button size="lg" className="gap-2 font-semibold shadow-md bg-[#F05323] hover:bg-[#d94417]">
                    <Briefcase className="w-4 h-4" />
                    Recruiter Portal <ArrowRight className="w-4 h-4" />
                  </Button>
                </Link>
                <Link to="/login?role=candidate">
                  <Button
                    variant="outline"
                    size="lg"
                    className="gap-2 font-semibold bg-white border-gray-300 text-gray-800 hover:bg-gray-50 shadow-xs"
                  >
                    <Terminal className="w-4 h-4 text-emerald-600" />
                    Candidate Portal
                  </Button>
                </Link>
              </div>

              {/* Highlights & Guarantees */}
              <div className="flex flex-wrap items-center gap-6 pt-4 text-xs text-gray-500 font-medium">
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                  <span>No manual question authoring</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <ShieldCheck className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                  <span>Docker-isolated container execution</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <CheckCheck className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                  <span>Objective 0–100 automated scoring</span>
                </div>
              </div>
            </div>

            {/* Hero Right: Candidate Java Spring Boot IDE Mockup */}
            <div className="lg:col-span-6">
              <div className="rounded-2xl border border-gray-200 bg-white shadow-xl overflow-hidden">
                {/* IDE Window Titlebar */}
                <div className="bg-gray-50 px-4 py-3 border-b border-gray-200 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="w-3 h-3 rounded-full bg-rose-400"></span>
                    <span className="w-3 h-3 rounded-full bg-amber-400"></span>
                    <span className="w-3 h-3 rounded-full bg-emerald-400"></span>
                    <span className="ml-3 text-xs font-semibold text-gray-700 flex items-center gap-1">
                      <FolderGit2 className="w-3.5 h-3.5 text-gray-500" />
                      notes-service (Spring Boot 3.x)
                    </span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-xs font-mono font-bold text-gray-700 bg-gray-200/80 px-2.5 py-1 rounded flex items-center gap-1">
                      <Clock className="w-3 h-3 text-[#F05323]" />
                      01:30:00 <span className="text-[10px] font-normal text-gray-500">Scheduled Window</span>
                    </span>
                    <div className="px-2.5 py-1 text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 rounded flex items-center gap-1">
                      <PlayCircle className="w-3.5 h-3.5" /> Docker Running
                    </div>
                  </div>
                </div>

                {/* IDE Body */}
                <div className="grid grid-cols-12 h-80 font-mono text-xs">
                  {/* File Explorer */}
                  <div className="col-span-4 bg-gray-50/80 border-r border-gray-200 p-3 space-y-1.5 text-gray-500 overflow-y-auto">
                    <div className="text-[10px] uppercase font-bold text-gray-400 tracking-wider mb-2">
                      Explorer
                    </div>
                    <div className="text-gray-800 font-semibold flex items-center gap-1">
                      📁 notes-service
                    </div>
                    <div className="pl-3 text-gray-700 font-medium">📁 src/main/java</div>
                    <div className="pl-6 text-gray-700 font-medium">📁 com.example.notes</div>
                    <div className="pl-9 text-[#F05323] font-bold flex items-center gap-1 bg-orange-50/80 py-1 px-1.5 rounded border border-orange-200/50">
                      ☕ NoteController.java
                    </div>
                    <div className="pl-9 text-gray-600">☕ NoteService.java</div>
                    <div className="pl-9 text-gray-600">☕ NoteRepository.java</div>
                    <div className="pl-9 text-gray-600">☕ NoteEntity.java</div>
                    <div className="pl-3 text-gray-700 font-medium">📁 src/main/resources</div>
                    <div className="pl-6 text-gray-500">📄 application.yml</div>
                    <div className="pl-3 text-gray-700 font-medium">📄 pom.xml</div>
                  </div>

                  {/* Code Area */}
                  <div className="col-span-8 p-4 bg-white space-y-1.5 overflow-y-auto text-gray-800 leading-relaxed select-none">
                    <div className="text-gray-400">// Feature: Implement GET /api/notes/search endpoint</div>
                    <div>
                      <span className="text-purple-600 font-semibold">@RestController</span>
                    </div>
                    <div>
                      <span className="text-purple-600 font-semibold">@RequestMapping</span>(
                      <span className="text-emerald-700">"/api/notes"</span>)
                    </div>
                    <div>
                      <span className="text-blue-600 font-semibold">public class</span>{" "}
                      <span className="text-gray-900 font-bold">NoteController</span> &#123;
                    </div>
                    <div className="pl-4">
                      <span className="text-purple-600 font-semibold">@GetMapping</span>(
                      <span className="text-emerald-700">"/search"</span>)
                    </div>
                    <div className="pl-4">
                      <span className="text-blue-600 font-semibold">public</span> ResponseEntity&lt;List&lt;Note&gt;&gt;{" "}
                      <span className="text-blue-700 font-semibold">searchNotes</span>(
                    </div>
                    <div className="pl-8">
                      <span className="text-purple-600 font-semibold">@RequestParam</span> String keyword) &#123;
                    </div>
                    <div className="pl-8 text-emerald-800 bg-emerald-50/70 border border-emerald-200 rounded px-2 py-0.5 my-0.5">
                      List&lt;Note&gt; results = noteService.searchByKeyword(keyword);
                    </div>
                    <div className="pl-8">
                      <span className="text-purple-600 font-semibold">return</span> ResponseEntity.ok(results);
                    </div>
                    <div className="pl-4">&#125;</div>
                    <div>&#125;</div>
                  </div>
                </div>

                {/* Terminal / Build Output */}
                <div className="bg-[#0F172A] text-gray-200 p-4 border-t border-gray-800 text-xs font-mono space-y-1">
                  <div className="text-gray-400 text-[10px] uppercase tracking-wider flex items-center justify-between pb-1 border-b border-gray-800">
                    <span className="text-white font-bold flex items-center gap-1.5">
                      <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
                      DOCKER RUNTIME LOGS
                    </span>
                    <span className="text-emerald-400 font-bold">PORT 8080 READY</span>
                  </div>
                  <div className="text-gray-400 pt-1">[INFO] Building notes-service 1.0.0 (Spring Boot 3.x)</div>
                  <div className="text-gray-400">[INFO] Compiling 4 source files to /target/classes</div>
                  <div className="text-emerald-400">[INFO] Tomcat started on port 8080 (http) with context path ''</div>
                  <div className="text-emerald-400 font-semibold">[INFO] Started NotesApplication in 1.942 seconds</div>
                  <div className="text-blue-300 font-semibold">✔ Application is running. Evaluation engine ready for HTTP verification.</div>
                </div>
              </div>

              {/* Sub-note below mockup */}
              <div className="mt-3 flex items-center justify-center gap-1.5 text-xs text-gray-500 italic">
                <span>⤷</span>
                <span>
                  Generates practical feature tasks directly from the candidate's actual Spring Boot architecture.
                </span>
              </div>
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------- */}
        {/* 3. Core Capabilities / Architectural Pillars         */}
        {/* ---------------------------------------------------- */}
        <section id="architecture" className="py-20 bg-white border-t border-gray-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <span className="text-xs uppercase tracking-wider font-bold text-[#F05323]">
                Core Assessment Architecture
              </span>
              <h2 className="text-3xl sm:text-4xl font-extrabold text-gray-900 mt-3">
                How Evidence Evaluates Real-World Engineering Mastery
              </h2>
              <p className="text-sm sm:text-base text-gray-600 mt-3">
                No artificial coding puzzles. Evidence tests real architectural comprehension and hands-on implementation against actual Java Spring Boot codebases.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {/* Pillar 1: Automated Repository Analysis */}
              <div className="rounded-2xl border border-gray-200/90 p-6 bg-white shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
                <div className="space-y-4">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 rounded-full bg-blue-600 text-white font-bold flex items-center justify-center text-sm shadow-sm">
                      1
                    </span>
                    <h3 className="font-bold text-gray-900 text-lg">Repository Analysis</h3>
                  </div>
                  <p className="text-sm text-gray-600 leading-relaxed">
                    Evidence clones the candidate's GitHub repository and automatically extracts controllers, entities, services, and endpoints from the Spring Boot project.
                  </p>

                  <div className="space-y-2 text-xs font-mono bg-gray-50/80 p-3 rounded-xl border border-gray-100">
                    <div className="flex justify-between items-center text-gray-700">
                      <span className="text-gray-500">Framework:</span>
                      <span className="font-semibold text-blue-700">Spring Boot 3.x</span>
                    </div>
                    <div className="flex justify-between items-center text-gray-700">
                      <span className="text-gray-500">Extraction:</span>
                      <span className="font-semibold text-emerald-700">Controllers, Repos, Entities</span>
                    </div>
                    <div className="flex justify-between items-center text-gray-700">
                      <span className="text-gray-500">Difficulty:</span>
                      <span className="font-semibold text-[#F05323]">Configurable (Easy/Med/Hard)</span>
                    </div>
                  </div>
                </div>

                <div className="mt-6 pt-4 border-t border-gray-100 flex items-center gap-2 text-xs font-semibold text-blue-600">
                  <Layers className="w-4 h-4" />
                  <span>Deep structural comprehension</span>
                </div>
              </div>

              {/* Pillar 2: AI Feature Generation */}
              <div className="rounded-2xl border border-gray-200/90 p-6 bg-white shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
                <div className="space-y-4">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 rounded-full bg-[#F05323] text-white font-bold flex items-center justify-center text-sm shadow-sm">
                      2
                    </span>
                    <h3 className="font-bold text-gray-900 text-lg">AI Feature Specification</h3>
                  </div>
                  <p className="text-sm text-gray-600 leading-relaxed">
                    Generates a realistic, project-specific feature specification complete with endpoint requirements, request payloads, response contracts, and constraints.
                  </p>

                  <div className="space-y-2 text-xs font-mono bg-gray-50/80 p-3 rounded-xl border border-gray-100">
                    <div className="flex justify-between items-center text-gray-700">
                      <span className="text-gray-500">Feature Task:</span>
                      <span className="font-semibold text-gray-900">Add Search API</span>
                    </div>
                    <div className="flex justify-between items-center text-gray-700">
                      <span className="text-gray-500">Target Endpoint:</span>
                      <span className="font-semibold text-purple-700">GET /api/notes/search</span>
                    </div>
                    <div className="flex justify-between items-center text-gray-700">
                      <span className="text-gray-500">Contract:</span>
                      <span className="font-semibold text-emerald-700">JSON Schema Validated</span>
                    </div>
                  </div>
                </div>

                <div className="mt-6 pt-4 border-t border-gray-100 flex items-center gap-2 text-xs font-semibold text-[#F05323]">
                  <Cpu className="w-4 h-4" />
                  <span>Practical application extension</span>
                </div>
              </div>

              {/* Pillar 3: Isolated Docker & Black-Box Tests */}
              <div className="rounded-2xl border border-gray-200/90 p-6 bg-white shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
                <div className="space-y-4">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 rounded-full bg-emerald-600 text-white font-bold flex items-center justify-center text-sm shadow-sm">
                      3
                    </span>
                    <h3 className="font-bold text-gray-900 text-lg">Docker & Black-Box Testing</h3>
                  </div>
                  <p className="text-sm text-gray-600 leading-relaxed">
                    Executes candidate code inside isolated Docker containers. Hidden HTTP test suites verify responses, status codes, and edge cases to compute an objective score.
                  </p>

                  <div className="space-y-2 text-xs font-mono bg-gray-50/80 p-3 rounded-xl border border-gray-100">
                    <div className="flex justify-between items-center text-gray-700">
                      <span className="text-gray-500">Runtime:</span>
                      <span className="font-semibold text-emerald-700">Isolated Container</span>
                    </div>
                    <div className="flex justify-between items-center text-gray-700">
                      <span className="text-gray-500">Verification:</span>
                      <span className="font-semibold text-blue-700">Black-Box HTTP Tests</span>
                    </div>
                    <div className="flex justify-between items-center text-gray-700">
                      <span className="text-gray-500">Scoring:</span>
                      <span className="font-semibold text-purple-700">Automated 0–100 Scale</span>
                    </div>
                  </div>
                </div>

                <div className="mt-6 pt-4 border-t border-gray-100 flex items-center gap-2 text-xs font-semibold text-emerald-600">
                  <Sparkles className="w-4 h-4" />
                  <span>Objective, unbiased evaluation</span>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------- */}
        {/* 4. How It Works Timeline Stepper                     */}
        {/* ---------------------------------------------------- */}
        <section id="how-it-works" className="py-20 bg-[#F9FAFB] border-t border-gray-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-2xl mx-auto mb-16">
              <span className="text-xs uppercase tracking-wider font-bold text-gray-500">
                HOW IT WORKS
              </span>
              <h2 className="text-3xl font-extrabold text-gray-900 mt-2">
                4-Step Technical Assessment Workflow
              </h2>
              <p className="text-sm text-gray-600 mt-2">
                Seamless, automated assessment flow from workspace setup to verified recruiter report.
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8 relative">
              {/* Step 1 */}
              <div className="flex flex-col items-start bg-white p-6 rounded-2xl border border-gray-200/80 shadow-sm space-y-3">
                <div className="flex items-center gap-3">
                  <span className="w-7 h-7 rounded-full bg-blue-600 text-white font-bold flex items-center justify-center text-xs">
                    1
                  </span>
                  <div className="w-9 h-9 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
                    <Users className="w-5 h-5" />
                  </div>
                </div>
                <h3 className="font-bold text-gray-900 text-base">Select Candidate</h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  Recruiter creates a recruitment workspace and selects an existing registered candidate by their unique email.
                </p>
              </div>

              {/* Step 2 */}
              <div className="flex flex-col items-start bg-white p-6 rounded-2xl border border-gray-200/80 shadow-sm space-y-3">
                <div className="flex items-center gap-3">
                  <span className="w-7 h-7 rounded-full bg-[#F05323] text-white font-bold flex items-center justify-center text-xs">
                    2
                  </span>
                  <div className="w-9 h-9 rounded-xl bg-orange-50 text-[#F05323] flex items-center justify-center">
                    <GithubIcon className="w-5 h-5" />
                  </div>
                </div>
                <h3 className="font-bold text-gray-900 text-base">Configure Repository</h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  Recruiter enters the candidate's GitHub Spring Boot repo, branch, difficulty, duration, and fixed schedule start/end time.
                </p>
              </div>

              {/* Step 3 */}
              <div className="flex flex-col items-start bg-white p-6 rounded-2xl border border-gray-200/80 shadow-sm space-y-3">
                <div className="flex items-center gap-3">
                  <span className="w-7 h-7 rounded-full bg-purple-600 text-white font-bold flex items-center justify-center text-xs">
                    3
                  </span>
                  <div className="w-9 h-9 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
                    <Cpu className="w-5 h-5" />
                  </div>
                </div>
                <h3 className="font-bold text-gray-900 text-base">AI Preparation</h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  Asynchronous AI pipeline analyzes project structure and generates feature specs and hidden black-box test cases.
                </p>
              </div>

              {/* Step 4 */}
              <div className="flex flex-col items-start bg-white p-6 rounded-2xl border border-gray-200/80 shadow-sm space-y-3">
                <div className="flex items-center gap-3">
                  <span className="w-7 h-7 rounded-full bg-emerald-600 text-white font-bold flex items-center justify-center text-xs">
                    4
                  </span>
                  <div className="w-9 h-9 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
                    <FileCheck2 className="w-5 h-5" />
                  </div>
                </div>
                <h3 className="font-bold text-gray-900 text-base">Code & Verification</h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  Candidate codes in browser Monaco IDE, tests in Docker, and submits. Hidden tests verify outputs and score the assessment.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------- */}
        {/* 5. Role Portals Breakdown (Recruiter vs Candidate)   */}
        {/* ---------------------------------------------------- */}
        <section id="portals" className="py-20 bg-white border-t border-gray-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-2xl mx-auto mb-16">
              <span className="text-xs uppercase tracking-wider font-bold text-[#F05323]">
                Role-Specific Portals
              </span>
              <h2 className="text-3xl font-extrabold text-gray-900 mt-2">
                Tailored Experiences for Recruiters & Candidates
              </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {/* Recruiter Card */}
              <div className="rounded-3xl border border-gray-200 bg-gray-50/50 p-8 flex flex-col justify-between space-y-6 hover:border-orange-200 transition-colors">
                <div className="space-y-4">
                  <div className="w-12 h-12 rounded-2xl bg-orange-100 text-[#F05323] flex items-center justify-center">
                    <Briefcase className="w-6 h-6" />
                  </div>
                  <h3 className="text-2xl font-extrabold text-gray-900">
                    For Recruiters & Hiring Managers
                  </h3>
                  <p className="text-sm text-gray-600 leading-relaxed">
                    Easily manage placement drives, organize candidates into workspaces, launch customized assessments, and review granular test outcome reports.
                  </p>

                  <ul className="space-y-2.5 text-xs text-gray-600 pt-2">
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                      <span>Workspace-based candidate management & email lookup</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                      <span>Configurable difficulty levels & scheduled time windows</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                      <span>Live async pipeline status (Cloning → Analysis → Tests)</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                      <span>Detailed evaluation reports with test-by-test outcomes</span>
                    </li>
                  </ul>
                </div>

                <div className="pt-4 border-t border-gray-200">
                  <Link to="/login?role=recruiter" className="block">
                    <Button className="w-full font-semibold gap-2 shadow-xs bg-[#F05323] hover:bg-[#d94417]">
                      Access Recruiter Portal <ArrowRight className="w-4 h-4" />
                    </Button>
                  </Link>
                </div>
              </div>

              {/* Candidate Card */}
              <div className="rounded-3xl border border-gray-200 bg-gray-50/50 p-8 flex flex-col justify-between space-y-6 hover:border-emerald-200 transition-colors">
                <div className="space-y-4">
                  <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
                    <Code2 className="w-6 h-6" />
                  </div>
                  <h3 className="text-2xl font-extrabold text-gray-900">
                    For Developers & Candidates
                  </h3>
                  <p className="text-sm text-gray-600 leading-relaxed">
                    Work on familiar project architectures without complex local toolchains. Take scheduled assessments inside a high-performance browser IDE.
                  </p>

                  <ul className="space-y-2.5 text-xs text-gray-600 pt-2">
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                      <span>Personalized dashboard for scheduled & completed assessments</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                      <span>Full-featured in-browser Monaco Editor for Java code</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                      <span>Live container execution & terminal build logs</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-emerald-600 flex-shrink-0" />
                      <span>Automated evaluation with score summary upon completion</span>
                    </li>
                  </ul>
                </div>

                <div className="pt-4 border-t border-gray-200">
                  <Link to="/login?role=candidate" className="block">
                    <Button
                      variant="outline"
                      className="w-full font-semibold gap-2 border-gray-300 text-gray-800 bg-white hover:bg-gray-50 shadow-xs"
                    >
                      Access Candidate Portal <ArrowRight className="w-4 h-4" />
                    </Button>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------- */}
        {/* 6. Call To Action (CTA) Banner                       */}
        {/* ---------------------------------------------------- */}
        <section className="py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
          <div className="rounded-3xl border border-gray-200 bg-white p-8 sm:p-12 shadow-md flex flex-col sm:flex-row items-center justify-between gap-8">
            <div className="flex items-center gap-6">
              <div className="w-16 h-16 rounded-2xl bg-orange-50 border border-orange-100 flex items-center justify-center text-2xl flex-shrink-0">
                🎯
              </div>
              <div className="space-y-1">
                <h3 className="text-2xl font-extrabold text-gray-900">
                  Ready to test real-world software capability?
                </h3>
                <p className="text-sm text-gray-600">
                  Transform Java Spring Boot repositories into practical coding assessments with Evidence.
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3 w-full sm:w-auto">
              <Link to="/signup?role=recruiter" className="w-full sm:w-auto">
                <Button size="lg" className="w-full sm:w-auto gap-2 font-semibold shadow-md bg-[#F05323] hover:bg-[#d94417]">
                  Start Recruiting <ArrowRight className="w-4 h-4" />
                </Button>
              </Link>
            </div>
          </div>
        </section>
      </main>

      {/* ---------------------------------------------------- */}
      {/* 7. Comprehensive Footer                              */}
      {/* ---------------------------------------------------- */}
      <footer className="bg-white border-t border-gray-200 pt-16 pb-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-12">
            {/* Column 1: Brand & Mission */}
            <div className="col-span-2 space-y-4">
              <Logo size="md" />
              <p className="text-xs text-gray-500 max-w-sm leading-relaxed">
                Evidence is the recruiter-focused technical assessment platform designed to verify whether a candidate can genuinely understand and extend a Java Spring Boot software project.
              </p>
              <div className="flex items-center space-x-3 pt-2 text-gray-400">
                <span className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center text-gray-600">
                  <GithubIcon className="w-4 h-4" />
                </span>
                <span className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center text-gray-600">
                  <LinkedinIcon className="w-4 h-4" />
                </span>
                <span className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center text-gray-600">
                  <TwitterIcon className="w-4 h-4" />
                </span>
                <span className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center text-gray-600">
                  <YoutubeIcon className="w-4 h-4" />
                </span>
              </div>
            </div>

            {/* Column 2: Navigation */}
            <div className="space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-gray-900">Platform</h4>
              <ul className="space-y-2 text-xs text-gray-500">
                <li><a href="#overview" className="hover:text-gray-900 transition-colors">Overview</a></li>
                <li><a href="#how-it-works" className="hover:text-gray-900 transition-colors">How It Works</a></li>
                <li><a href="#architecture" className="hover:text-gray-900 transition-colors">Core Capabilities</a></li>
                <li><a href="#portals" className="hover:text-gray-900 transition-colors">Role Portals</a></li>
              </ul>
            </div>

            {/* Column 3: Portals */}
            <div className="space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-gray-900">Portals</h4>
              <ul className="space-y-2 text-xs text-gray-500">
                <li><Link to="/login?role=recruiter" className="hover:text-gray-900 transition-colors">Recruiter Login</Link></li>
                <li><Link to="/login?role=candidate" className="hover:text-gray-900 transition-colors">Candidate Login</Link></li>
                <li><Link to="/signup?role=recruiter" className="hover:text-gray-900 transition-colors">Recruiter Sign Up</Link></li>
                <li><Link to="/signup?role=candidate" className="hover:text-gray-900 transition-colors">Candidate Sign Up</Link></li>
              </ul>
            </div>
          </div>

          <div className="border-t border-gray-100 pt-8 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 gap-4">
            <p>© 2026 EVIDENCE. Recruiter & Candidate Technical Assessment Platform. All rights reserved.</p>
            <div className="flex space-x-6">
              <span className="text-gray-500">PostgreSQL • Java Spring Boot • Docker Sandbox</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};
