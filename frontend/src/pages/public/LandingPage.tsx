import { useState } from "react";
import { Link } from "react-router-dom";
import { Logo } from "@/components/ui/logo";
import { Button } from "@/components/ui/button";
import {
  ArrowRight,
  Play,
  CheckCircle2,
  Terminal,
  Bug,
  Cpu,
  Layers,
  X,
  Sparkles,
} from "lucide-react";
import {
  GithubIcon,
  LinkedinIcon,
  TwitterIcon,
  YoutubeIcon,
} from "@/components/ui/social-icons";

export const LandingPage = () => {
  const [isVideoModalOpen, setIsVideoModalOpen] = useState(false);

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col font-sans">
      {/* ---------------------------------------------------- */}
      {/* 1. Public Top Navigation                             */}
      {/* ---------------------------------------------------- */}
      <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <Logo size="md" />

          <nav className="hidden md:flex items-center space-x-8 text-sm font-medium text-gray-600">
            <a href="#product" className="hover:text-gray-900 transition-colors">
              Product
            </a>
            <a href="#how-it-works" className="hover:text-gray-900 transition-colors">
              How It Works
            </a>
            <a href="#features" className="hover:text-gray-900 transition-colors">
              Features
            </a>
            <a href="#for-companies" className="hover:text-gray-900 transition-colors">
              For Companies
            </a>
            <a href="#pricing" className="hover:text-gray-900 transition-colors">
              Pricing
            </a>
            <a href="#resources" className="hover:text-gray-900 transition-colors">
              Resources
            </a>
          </nav>

          <div className="flex items-center space-x-4">
            <Link to="/login">
              <Button variant="outline" size="sm" className="font-semibold text-gray-700">
                Log in
              </Button>
            </Link>
            <Link to="/signup">
              <Button size="sm" className="font-semibold shadow-sm">
                Get Started
              </Button>
            </Link>
          </div>
        </div>
      </header>

      {/* ---------------------------------------------------- */}
      {/* 2. Hero Section with Live IDE Preview                */}
      {/* ---------------------------------------------------- */}
      <main className="flex-1">
        <section className="py-16 sm:py-24 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            {/* Hero Left Content */}
            <div className="lg:col-span-6 space-y-6">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-orange-50 text-[#F05323] border border-orange-200/60">
                <span className="w-1.5 h-1.5 rounded-full bg-[#F05323] animate-pulse"></span>
                AI-Powered • Fullstack • Real-World
              </div>

              <h1 className="text-5xl sm:text-6xl font-extrabold text-gray-900 tracking-tight leading-[1.1]">
                Verify <br />
                <span className="text-[#F05323]">Real-World</span> <br />
                Mastery
              </h1>

              <p className="text-base sm:text-lg text-gray-600 max-w-xl leading-relaxed">
                Automatically assess candidates by injecting real bugs into their actual GitHub projects and evaluating their debugging skills across Data Flow, Syntax, and Business Logic.
              </p>

              <div className="flex flex-wrap items-center gap-4 pt-2">
                <Link to="/signup">
                  <Button size="lg" className="gap-2 font-semibold shadow-md">
                    Get Started <ArrowRight className="w-4 h-4" />
                  </Button>
                </Link>
                <Button
                  variant="outline"
                  size="lg"
                  className="gap-2 font-semibold bg-white"
                  onClick={() => setIsVideoModalOpen(true)}
                >
                  Watch Demo <Play className="w-4 h-4 text-gray-500 fill-gray-500" />
                </Button>
              </div>

              <div className="flex items-center gap-6 pt-4 text-xs text-gray-500 font-medium">
                <div className="flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                  <span>No setup for candidates</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <Terminal className="w-4 h-4 text-emerald-600" />
                  <span>Works with any tech stack</span>
                </div>
              </div>
            </div>

            {/* Hero Right: Candidate IDE Mockup */}
            <div className="lg:col-span-6">
              <div className="rounded-2xl border border-gray-200 bg-white shadow-xl overflow-hidden">
                {/* IDE Window Titlebar */}
                <div className="bg-gray-50 px-4 py-3 border-b border-gray-200 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="w-3 h-3 rounded-full bg-rose-400"></span>
                    <span className="w-3 h-3 rounded-full bg-amber-400"></span>
                    <span className="w-3 h-3 rounded-full bg-emerald-400"></span>
                    <span className="ml-3 text-xs font-semibold text-gray-700 flex items-center gap-1">
                      E-Commerce Platform <span className="text-[10px] text-gray-400">▾</span>
                    </span>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-xs font-mono font-bold text-gray-700 bg-gray-200/80 px-2 py-0.5 rounded">
                      02:45:30 <span className="text-[10px] font-normal text-gray-500">Time Left</span>
                    </span>
                    <Button size="sm" className="h-7 text-xs font-semibold">
                      Run Build
                    </Button>
                  </div>
                </div>

                {/* IDE Body */}
                <div className="grid grid-cols-12 h-80 font-mono text-xs">
                  {/* File Explorer */}
                  <div className="col-span-4 bg-gray-50/70 border-r border-gray-200 p-3 space-y-1.5 text-gray-500 overflow-y-auto">
                    <div className="text-[10px] uppercase font-bold text-gray-400 tracking-wider mb-2">
                      Explorer
                    </div>
                    <div className="text-gray-700 font-medium">📁 e-commerce-platform</div>
                    <div className="pl-3 text-gray-500">📁 frontend</div>
                    <div className="pl-3 text-gray-800 font-semibold">📁 backend</div>
                    <div className="pl-6 text-gray-700 font-medium">📁 src</div>
                    <div className="pl-9 text-gray-700 font-medium">📁 controllers</div>
                    <div className="pl-12 text-[#F05323] font-bold flex items-center gap-1 bg-orange-50/60 py-0.5 rounded">
                      📄 userController.js ✕
                    </div>
                    <div className="pl-9 text-gray-500">📁 routes</div>
                    <div className="pl-9 text-gray-500">📁 models</div>
                    <div className="pl-9 text-gray-500">📁 services</div>
                    <div className="pl-9 text-gray-500">📁 tests</div>
                    <div className="pl-3 text-gray-500">📄 package.json</div>
                    <div className="pl-3 text-gray-500">📄 README.md</div>
                  </div>

                  {/* Code Area */}
                  <div className="col-span-8 p-4 bg-white space-y-1.5 overflow-y-auto text-gray-800 leading-relaxed select-none">
                    <div className="text-gray-400">// Get user by ID</div>
                    <div>
                      <span className="text-purple-600 font-semibold">exports</span>.
                      <span className="text-blue-600 font-semibold">getUserById</span> ={" "}
                      <span className="text-purple-600 font-semibold">async</span> (req, res) =&gt; &#123;
                    </div>
                    <div className="pl-4">
                      <span className="text-purple-600 font-semibold">try</span> &#123;
                    </div>
                    <div className="pl-8">
                      <span className="text-purple-600">const</span> user ={" "}
                      <span className="text-purple-600">await</span> User.findById(req.params.id);
                    </div>
                    <div className="pl-8">
                      <span className="text-purple-600">if</span> (!user) &#123;
                    </div>
                    <div className="pl-12 text-rose-600 bg-rose-50 border border-rose-100 rounded px-1.5 py-0.5 flex items-center justify-between">
                      <span>return res.status(404).json(&#123; message: 'User not found' &#125;);</span>
                      <span className="w-2 h-2 rounded-full bg-rose-500"></span>
                    </div>
                    <div className="pl-8">&#125;</div>
                    <div className="pl-8">
                      return res.status(200).json(&#123; success: true, data: user &#125;);
                    </div>
                    <div className="pl-4">
                      &#125; <span className="text-purple-600 font-semibold">catch</span> (error) &#123;
                    </div>
                    <div className="pl-8">
                      return res.status(500).json(&#123; success: false, message: error.message &#125;);
                    </div>
                    <div className="pl-4">&#125;</div>
                    <div>&#125;;</div>
                  </div>
                </div>

                {/* Terminal / Build Output */}
                <div className="bg-[#0F172A] text-gray-200 p-4 border-t border-gray-800 text-xs font-mono space-y-1.5">
                  <div className="text-gray-400 text-[10px] uppercase tracking-wider flex items-center gap-4">
                    <span className="text-white font-bold border-b border-orange-500 pb-0.5">TERMINAL</span>
                    <span className="text-gray-500 cursor-pointer">BUILD OUTPUT</span>
                  </div>
                  <div className="text-rose-400 pt-1">Error: Cannot GET /api/users/123</div>
                  <div className="text-gray-400 pl-4">at Layer.handle (node_modules/express/lib/router/layer.js:95:5)</div>
                  <div className="text-gray-400 pl-4">at next (node_modules/express/lib/router/route.js:144:13)</div>
                  <div className="text-rose-500 font-bold pt-1">✗ Build failed with exit code 1</div>
                </div>
              </div>

              {/* Sub-note below mockup matching UI image */}
              <div className="mt-3 flex items-center justify-center gap-1.5 text-xs text-gray-500 italic">
                <span>⤷</span>
                <span>
                  No red squiggly lines. Use <strong className="text-gray-800 not-italic">terminal logs</strong> to find the bug.
                </span>
              </div>
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------- */}
        {/* 3. Core Evaluation Taxonomy                          */}
        {/* ---------------------------------------------------- */}
        <section id="features" className="py-20 bg-white border-t border-gray-100">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="text-center max-w-3xl mx-auto mb-16">
              <span className="text-xs uppercase tracking-wider font-bold text-[#F05323]">
                Our Core Evaluation Taxonomy
              </span>
              <h2 className="text-3xl sm:text-4xl font-extrabold text-gray-900 mt-3">
                We inject 3 types of bugs to evaluate real debugging skills.
              </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {/* 1: Data Flow Bugs */}
              <div className="rounded-2xl border border-gray-200/90 p-6 bg-white shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
                <div className="space-y-4">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 rounded-full bg-blue-600 text-white font-bold flex items-center justify-center text-sm shadow-sm">
                      1
                    </span>
                    <h3 className="font-bold text-gray-900 text-lg">Data Flow Bugs</h3>
                  </div>
                  <p className="text-sm text-gray-600 leading-relaxed min-h-[40px]">
                    Issues in APIs, routes, data mapping, or incorrect flow between components.
                  </p>

                  <div className="grid grid-cols-2 gap-2 text-xs font-mono bg-gray-50/80 p-3 rounded-xl border border-gray-100">
                    <div>
                      <div className="text-[10px] text-gray-400 uppercase font-semibold mb-1">Before</div>
                      <div className="text-emerald-700 truncate font-medium">app.get('/api/users/:id')</div>
                    </div>
                    <div>
                      <div className="text-[10px] text-gray-400 uppercase font-semibold mb-1">After (Injected Bug)</div>
                      <div className="text-rose-600 truncate font-medium flex items-center justify-between">
                        app.get('/api/user/:id') <span className="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="mt-6 pt-4 border-t border-gray-100 flex items-center gap-2 text-xs font-semibold text-blue-600">
                  <Layers className="w-4 h-4" />
                  <span>Tests integration & system thinking</span>
                </div>
              </div>

              {/* 2: Syntax / Structural Bugs */}
              <div className="rounded-2xl border border-gray-200/90 p-6 bg-white shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
                <div className="space-y-4">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 rounded-full bg-[#F05323] text-white font-bold flex items-center justify-center text-sm shadow-sm">
                      2
                    </span>
                    <h3 className="font-bold text-gray-900 text-lg">Syntax / Structural Bugs</h3>
                  </div>
                  <p className="text-sm text-gray-600 leading-relaxed min-h-[40px]">
                    Syntax errors, typos, or structural issues that break the application.
                  </p>

                  <div className="grid grid-cols-2 gap-2 text-xs font-mono bg-gray-50/80 p-3 rounded-xl border border-gray-100">
                    <div>
                      <div className="text-[10px] text-gray-400 uppercase font-semibold mb-1">Before</div>
                      <div className="text-emerald-700 font-medium">function total(a, b)</div>
                    </div>
                    <div>
                      <div className="text-[10px] text-gray-400 uppercase font-semibold mb-1">After (Injected Bug)</div>
                      <div className="text-rose-600 font-medium flex items-center justify-between">
                        functoin total(a, b) <span className="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="mt-6 pt-4 border-t border-gray-100 flex items-center gap-2 text-xs font-semibold text-[#F05323]">
                  <Cpu className="w-4 h-4" />
                  <span>Tests coding accuracy & attention to detail</span>
                </div>
              </div>

              {/* 3: Business Logic Bugs */}
              <div className="rounded-2xl border border-gray-200/90 p-6 bg-white shadow-sm hover:shadow-md transition-all flex flex-col justify-between">
                <div className="space-y-4">
                  <div className="flex items-center gap-3">
                    <span className="w-8 h-8 rounded-full bg-rose-600 text-white font-bold flex items-center justify-center text-sm shadow-sm">
                      3
                    </span>
                    <h3 className="font-bold text-gray-900 text-lg">Business Logic Bugs</h3>
                  </div>
                  <p className="text-sm text-gray-600 leading-relaxed min-h-[40px]">
                    Flawed logic, incorrect conditions, or algorithmic mistakes.
                  </p>

                  <div className="grid grid-cols-2 gap-2 text-xs font-mono bg-gray-50/80 p-3 rounded-xl border border-gray-100">
                    <div>
                      <div className="text-[10px] text-gray-400 uppercase font-semibold mb-1">Before</div>
                      <div className="text-emerald-700 font-medium">if (total &gt; 5000)</div>
                    </div>
                    <div>
                      <div className="text-[10px] text-gray-400 uppercase font-semibold mb-1">After (Injected Bug)</div>
                      <div className="text-rose-600 font-medium flex items-center justify-between">
                        if (total &lt; 5000) <span className="w-1.5 h-1.5 rounded-full bg-rose-500"></span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="mt-6 pt-4 border-t border-gray-100 flex items-center gap-2 text-xs font-semibold text-rose-600">
                  <Bug className="w-4 h-4" />
                  <span>Tests problem-solving & logic</span>
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
                Effortless 4-Step Technical Assessment
              </h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8 relative">
              {/* Step 1 */}
              <div className="flex flex-col items-start bg-white p-6 rounded-2xl border border-gray-200/80 shadow-sm space-y-3">
                <div className="flex items-center gap-3">
                  <span className="w-7 h-7 rounded-full bg-blue-600 text-white font-bold flex items-center justify-center text-xs">
                    1
                  </span>
                  <div className="w-9 h-9 rounded-xl bg-gray-100 flex items-center justify-center text-gray-800">
                    <GithubIcon className="w-5 h-5" />
                  </div>
                </div>
                <h3 className="font-bold text-gray-900 text-base">Connect GitHub</h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  Candidate submits their real GitHub repository link and branch.
                </p>
              </div>

              {/* Step 2 */}
              <div className="flex flex-col items-start bg-white p-6 rounded-2xl border border-gray-200/80 shadow-sm space-y-3">
                <div className="flex items-center gap-3">
                  <span className="w-7 h-7 rounded-full bg-[#F05323] text-white font-bold flex items-center justify-center text-xs">
                    2
                  </span>
                  <div className="w-9 h-9 rounded-xl bg-orange-50 text-[#F05323] flex items-center justify-center">
                    <Bug className="w-5 h-5" />
                  </div>
                </div>
                <h3 className="font-bold text-gray-900 text-base">We Inject Bugs</h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  EVIDENCE injects 3 real bugs across our automated taxonomy.
                </p>
              </div>

              {/* Step 3 */}
              <div className="flex flex-col items-start bg-white p-6 rounded-2xl border border-gray-200/80 shadow-sm space-y-3">
                <div className="flex items-center gap-3">
                  <span className="w-7 h-7 rounded-full bg-emerald-600 text-white font-bold flex items-center justify-center text-xs">
                    3
                  </span>
                  <div className="w-9 h-9 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
                    <Terminal className="w-5 h-5" />
                  </div>
                </div>
                <h3 className="font-bold text-gray-900 text-base">Candidate Fixes</h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  Candidate identifies and fixes the bugs directly inside the isolated IDE.
                </p>
              </div>

              {/* Step 4 */}
              <div className="flex flex-col items-start bg-white p-6 rounded-2xl border border-gray-200/80 shadow-sm space-y-3">
                <div className="flex items-center gap-3">
                  <span className="w-7 h-7 rounded-full bg-purple-600 text-white font-bold flex items-center justify-center text-xs">
                    4
                  </span>
                  <div className="w-9 h-9 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
                    <Sparkles className="w-5 h-5" />
                  </div>
                </div>
                <h3 className="font-bold text-gray-900 text-base">AI Evaluates</h3>
                <p className="text-xs text-gray-500 leading-relaxed">
                  AI evaluates time taken, code quality, test coverage, and generates deep insights.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* ---------------------------------------------------- */}
        {/* 5. Call To Action (CTA) Banner                       */}
        {/* ---------------------------------------------------- */}
        <section className="py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
          <div className="rounded-3xl border border-gray-200 bg-white p-8 sm:p-12 shadow-md flex flex-col sm:flex-row items-center justify-between gap-8">
            <div className="flex items-center gap-6">
              <div className="w-16 h-16 rounded-2xl bg-orange-50 border border-orange-100 flex items-center justify-center text-2xl flex-shrink-0">
                🚀
              </div>
              <div className="space-y-1">
                <h3 className="text-2xl font-extrabold text-gray-900">
                  Ready to assess real-world developers?
                </h3>
                <p className="text-sm text-gray-600">
                  Join 500+ companies using EVIDENCE to hire with confidence.
                </p>
              </div>
            </div>

            <Link to="/signup">
              <Button size="lg" className="gap-2 font-semibold whitespace-nowrap shadow-md">
                Get Started <ArrowRight className="w-4 h-4" />
              </Button>
            </Link>
          </div>
        </section>
      </main>

      {/* ---------------------------------------------------- */}
      {/* 6. Comprehensive Footer                              */}
      {/* ---------------------------------------------------- */}
      <footer className="bg-white border-t border-gray-200 pt-16 pb-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-5 gap-8 mb-12">
            {/* Column 1: Brand & Bio */}
            <div className="col-span-2 space-y-4">
              <Logo size="md" />
              <p className="text-xs text-gray-500 max-w-sm leading-relaxed">
                The leading AI-powered platform for real-world technical assessments. We help companies hire developers who can truly code and debug.
              </p>
              <div className="flex items-center space-x-3 pt-2 text-gray-400">
                <a href="#" className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center hover:text-gray-900 hover:bg-gray-200 transition-colors">
                  <GithubIcon className="w-4 h-4" />
                </a>
                <a href="#" className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center hover:text-gray-900 hover:bg-gray-200 transition-colors">
                  <LinkedinIcon className="w-4 h-4" />
                </a>
                <a href="#" className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center hover:text-gray-900 hover:bg-gray-200 transition-colors">
                  <TwitterIcon className="w-4 h-4" />
                </a>
                <a href="#" className="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center hover:text-gray-900 hover:bg-gray-200 transition-colors">
                  <YoutubeIcon className="w-4 h-4" />
                </a>
              </div>
            </div>

            {/* Column 2: Product */}
            <div className="space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-gray-900">Product</h4>
              <ul className="space-y-2 text-xs text-gray-500">
                <li><a href="#" className="hover:text-gray-900">Features</a></li>
                <li><a href="#" className="hover:text-gray-900">How It Works</a></li>
                <li><a href="#" className="hover:text-gray-900">Pricing</a></li>
                <li><a href="#" className="hover:text-gray-900">Security</a></li>
                <li><a href="#" className="hover:text-gray-900">Integrations</a></li>
              </ul>
            </div>

            {/* Column 3: Company */}
            <div className="space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-gray-900">Company</h4>
              <ul className="space-y-2 text-xs text-gray-500">
                <li><a href="#" className="hover:text-gray-900">About Us</a></li>
                <li><a href="#" className="hover:text-gray-900">Careers</a></li>
                <li><a href="#" className="hover:text-gray-900">Blog</a></li>
                <li><a href="#" className="hover:text-gray-900">Contact Us</a></li>
                <li><a href="#" className="hover:text-gray-900">Press Kit</a></li>
              </ul>
            </div>

            {/* Column 4: Resources */}
            <div className="space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-gray-900">Resources</h4>
              <ul className="space-y-2 text-xs text-gray-500">
                <li><a href="#" className="hover:text-gray-900">Docs</a></li>
                <li><a href="#" className="hover:text-gray-900">Help Center</a></li>
                <li><a href="#" className="hover:text-gray-900">Guides</a></li>
                <li><a href="#" className="hover:text-gray-900">API Reference</a></li>
                <li><a href="#" className="hover:text-gray-900">Status</a></li>
              </ul>
            </div>
          </div>

          <div className="border-t border-gray-100 pt-8 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-400 gap-4">
            <p>© 2026 EVIDENCE. All rights reserved.</p>
            <div className="flex space-x-6">
              <a href="#" className="hover:text-gray-600">Privacy Policy</a>
              <a href="#" className="hover:text-gray-600">Terms of Service</a>
              <a href="#" className="hover:text-gray-600">Cookie Policy</a>
              <a href="#" className="hover:text-gray-600">Refund Policy</a>
            </div>
          </div>
        </div>
      </footer>

      {/* Video Demo Modal */}
      {isVideoModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full p-6 space-y-4 shadow-2xl relative">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-lg text-gray-900">EVIDENCE Platform Walkthrough</h3>
              <button
                onClick={() => setIsVideoModalOpen(false)}
                className="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center text-gray-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
            <div className="aspect-video bg-gray-950 rounded-xl flex items-center justify-center text-gray-400">
              <div className="text-center space-y-2">
                <Play className="w-12 h-12 mx-auto text-[#F05323]" />
                <p className="text-sm font-medium">Interactive Demo Video</p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
