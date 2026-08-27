import { useState, useEffect } from "react";
import {
  Binary,
  Code2,
  FolderTree,
  Sparkles,
  CheckCircle2,
  X,
  ChevronRight,
  Database,
  Layers,
  Copy,
  Check,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { assessmentService } from "@/services/assessmentService";

interface AstAnalysisDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  assessmentTitle: string;
  assessmentId?: string;
}

export const AstAnalysisDrawer = ({
  isOpen,
  onClose,
  assessmentTitle,
  assessmentId,
}: AstAnalysisDrawerProps) => {
  const [activeTab, setActiveTab] = useState<"FEATURE" | "AST_STRUCTURE" | "ENDPOINTS">("FEATURE");
  const [copied, setCopied] = useState(false);
  const [realSpec, setRealSpec] = useState<any>(null);
  const [realAnalysis, setRealAnalysis] = useState<any>(null);

  useEffect(() => {
    let isMounted = true;
    if (isOpen && assessmentId) {
      assessmentService.getFeatureSpec(assessmentId)
        .then((spec) => {
          if (isMounted && spec) setRealSpec(spec);
        })
        .catch(() => {});

      assessmentService.getRepositoryAnalysis(assessmentId)
        .then((analysis) => {
          if (isMounted && analysis) setRealAnalysis(analysis);
        })
        .catch(() => {});
    }
    return () => {
      isMounted = false;
    };
  }, [isOpen, assessmentId]);

  if (!isOpen) return null;

  const rawSpec = realSpec || {};
  const rawAnalysis = realAnalysis || {};

  // Normalized spec
  const specTitle = rawSpec?.featureName || rawSpec?.title || assessmentTitle || "Java Spring Boot Feature Specification";
  const specDescription = rawSpec?.description || "Implement the specified API feature according to the test suite specifications.";
  const specHttpMethod = rawSpec?.httpMethod || (rawSpec?.requestSpecification?.httpMethod) || "GET";
  const specEndpoint = rawSpec?.endpoint || (rawSpec?.requestSpecification?.endpoint) || "/api/v1/resource";

  // Robust array conversion for requirements
  let specRequirements: string[] = [];
  if (Array.isArray(rawSpec?.requirements)) {
    specRequirements = rawSpec.requirements;
  } else if (typeof rawSpec?.requirements === "string") {
    try {
      const parsed = JSON.parse(rawSpec.requirements);
      specRequirements = Array.isArray(parsed) ? parsed : [rawSpec.requirements];
    } catch {
      specRequirements = [rawSpec.requirements];
    }
  } else if (rawSpec?.requirements && typeof rawSpec.requirements === "object") {
    specRequirements = Array.isArray(rawSpec.requirements.items) ? rawSpec.requirements.items : Object.values(rawSpec.requirements);
  } else {
    specRequirements = ["Implement the requested feature endpoint", "Ensure standard HTTP response status codes"];
  }

  // Robust array conversion for constraints
  let specConstraints: string[] = [];
  if (Array.isArray(rawSpec?.constraints)) {
    specConstraints = rawSpec.constraints;
  } else if (typeof rawSpec?.constraints === "string") {
    try {
      const parsed = JSON.parse(rawSpec.constraints);
      specConstraints = Array.isArray(parsed) ? parsed : [rawSpec.constraints];
    } catch {
      specConstraints = [rawSpec.constraints];
    }
  } else if (rawSpec?.constraints && typeof rawSpec.constraints === "object") {
    specConstraints = Array.isArray(rawSpec.constraints.items) ? rawSpec.constraints.items : Object.values(rawSpec.constraints);
  } else {
    specConstraints = ["Validate input payloads", "Follow Spring Boot best practices"];
  }

  const specRequestStr = typeof rawSpec?.requestSpecification === "object"
    ? JSON.stringify(rawSpec.requestSpecification, null, 2)
    : (rawSpec?.requestSpecification || "GET /api/v1/resource");
  const specResponseStr = typeof rawSpec?.responseSpecification === "object"
    ? JSON.stringify(rawSpec.responseSpecification, null, 2)
    : (rawSpec?.responseSpecification || "HTTP 200 OK with JSON response");

  // Normalized AST analysis
  let controllers: any[] = [];
  let services: any[] = [];
  let entities: any[] = [];
  let repositories: any[] = [];
  let projectMeta: any = null;
  let dependencies: string[] = [];
  let endpoints: any[] = [];

  try {
    const src = typeof rawAnalysis?.sourceCodeStructure === "string"
      ? JSON.parse(rawAnalysis.sourceCodeStructure)
      : rawAnalysis?.sourceCodeStructure;
    controllers = Array.isArray(src?.controllers) ? src.controllers : [];
    services = Array.isArray(src?.services) ? src.services : [];
    entities = Array.isArray(src?.entities) ? src.entities : [];
    repositories = Array.isArray(src?.repositories) ? src.repositories : [];

    const proj = typeof rawAnalysis?.projectStructure === "string"
      ? JSON.parse(rawAnalysis.projectStructure)
      : rawAnalysis?.projectStructure;
    projectMeta = proj;
    dependencies = Array.isArray(proj?.dependencies) ? proj.dependencies : [];

    // Collect all controller endpoints if present
    controllers.forEach((c) => {
      if (c.endpoints && Array.isArray(c.endpoints)) {
        c.endpoints.forEach((ep: any) => {
          endpoints.push({
            method: ep.httpMethod || "GET",
            path: ep.fullPath || ep.path || `/${c.basePath || ""}`,
            controllerMethod: `${c.className}.${ep.handlerMethod || "handler"}()`,
            returnType: ep.returnType || "ResponseEntity",
          });
        });
      }
    });

    // If no controller endpoints array, check contentDetails
    if (endpoints.length === 0) {
      const cnt = typeof rawAnalysis?.contentDetails === "string"
        ? JSON.parse(rawAnalysis.contentDetails)
        : rawAnalysis?.contentDetails;
      if (cnt?.endpoints && Array.isArray(cnt.endpoints)) {
        endpoints = cnt.endpoints.map((ep: any) => {
          if (typeof ep === "string") {
            const parts = ep.split(" ");
            return {
              method: parts[0] || "GET",
              path: parts[1] || ep,
              controllerMethod: "Controller Handler",
              returnType: "JSON",
            };
          }
          return ep;
        });
      }
    }
  } catch (err) {
    console.debug("AST parsing error:", err);
  }

  const handleCopySpec = () => {
    navigator.clipboard.writeText(JSON.stringify({ featureSpec: rawSpec, astAnalysis: rawAnalysis }, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl max-w-4xl w-full p-6 sm:p-8 shadow-2xl border border-gray-100 space-y-5 animate-in zoom-in-95 duration-200 max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-gray-100">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-orange-50 text-[#F05323] flex items-center justify-center font-bold">
              <Binary className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-lg font-extrabold text-gray-900">
                  Codebase AST & AI Feature Specification
                </h3>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800">
                  LIVE AST PARSER
                </span>
              </div>
              <p className="text-xs text-gray-500 font-mono">
                {assessmentTitle}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleCopySpec}
              className="text-xs font-semibold gap-1.5"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? "Copied" : "Copy Complete JSON"}</span>
            </Button>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center text-gray-500 transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Tab Buttons */}
        <div className="flex items-center gap-2 border-b border-gray-100 pb-2">
          <button
            onClick={() => setActiveTab("FEATURE")}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 ${
              activeTab === "FEATURE"
                ? "bg-[#F05323] text-white shadow-xs"
                : "text-gray-600 hover:bg-gray-100"
            }`}
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>AI Feature Specification</span>
          </button>

          <button
            onClick={() => setActiveTab("AST_STRUCTURE")}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 ${
              activeTab === "AST_STRUCTURE"
                ? "bg-[#F05323] text-white shadow-xs"
                : "text-gray-600 hover:bg-gray-100"
            }`}
          >
            <Layers className="w-3.5 h-3.5" />
            <span>Discovered AST Architecture</span>
          </button>

          <button
            onClick={() => setActiveTab("ENDPOINTS")}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 ${
              activeTab === "ENDPOINTS"
                ? "bg-[#F05323] text-white shadow-xs"
                : "text-gray-600 hover:bg-gray-100"
            }`}
          >
            <Code2 className="w-3.5 h-3.5" />
            <span>Discovered Endpoints ({endpoints.length})</span>
          </button>
        </div>

        {/* Tab Content */}
        <div className="flex-1 overflow-y-auto pr-1 space-y-4">
          {activeTab === "FEATURE" && (
            <div className="space-y-4">
              <div className="p-4 bg-orange-50/50 rounded-2xl border border-orange-200/80 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-bold uppercase tracking-wider text-[#F05323] block">
                    AI Feature Specification
                  </span>
                  <div className="flex items-center gap-1.5 font-mono text-xs">
                    <span className="px-2 py-0.5 rounded bg-[#F05323] text-white font-bold text-[10px]">
                      {specHttpMethod}
                    </span>
                    <span className="text-gray-900 font-bold">{specEndpoint}</span>
                  </div>
                </div>
                <h4 className="text-base font-extrabold text-gray-900">
                  {specTitle}
                </h4>
                <p className="text-xs text-gray-700 leading-relaxed">
                  {specDescription}
                </p>
              </div>

              {/* Endpoint Contract */}
              <div className="p-4 bg-gray-900 text-white rounded-2xl space-y-2 font-mono text-xs">
                <span className="text-[10px] font-bold text-gray-400 uppercase block font-sans">
                  Target REST API Contract
                </span>
                <div className="flex items-center gap-2">
                  <span className="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold">
                    {specHttpMethod}
                  </span>
                  <span className="text-amber-300 font-bold">{specEndpoint}</span>
                </div>
                {specRequestStr && (
                  <div className="text-gray-300 pt-2 text-[11px] bg-black/40 p-2.5 rounded-xl whitespace-pre-wrap">
                    <strong className="text-emerald-400">Request Specification:</strong>
                    {"\n"}{specRequestStr}
                  </div>
                )}
                {specResponseStr && (
                  <div className="text-gray-300 pt-1 text-[11px] bg-black/40 p-2.5 rounded-xl whitespace-pre-wrap">
                    <strong className="text-cyan-400">Expected Response Specification:</strong>
                    {"\n"}{specResponseStr}
                  </div>
                )}
              </div>

              {/* Requirements Checklist */}
              {specRequirements.length > 0 && (
                <div className="space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 uppercase tracking-wider">
                    Functional Requirements
                  </h5>
                  <ul className="space-y-1.5">
                    {specRequirements.map((req: string, i: number) => (
                      <li key={i} className="flex items-start gap-2 text-xs text-gray-700 bg-gray-50/80 p-2.5 rounded-xl border border-gray-100">
                        <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                        <span>{req}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Constraints */}
              {specConstraints.length > 0 && (
                <div className="space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 uppercase tracking-wider">
                    Architectural Constraints
                  </h5>
                  <ul className="space-y-1.5">
                    {specConstraints.map((c: string, i: number) => (
                      <li key={i} className="flex items-start gap-2 text-xs text-gray-600 bg-gray-50/80 p-2.5 rounded-xl border border-gray-100">
                        <ChevronRight className="w-4 h-4 text-orange-500 shrink-0 mt-0.5" />
                        <span>{c}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}

          {activeTab === "AST_STRUCTURE" && (
            <div className="space-y-4">
              {/* Project Meta */}
              {projectMeta && (
                <div className="p-4 bg-gray-900 text-white rounded-2xl flex flex-wrap items-center justify-between gap-3 text-xs font-mono">
                  <div>
                    <span className="text-gray-400 block text-[10px] uppercase font-sans">Framework</span>
                    <strong className="text-emerald-400">Spring Boot {projectMeta.springBootVersion || "3.x"}</strong>
                  </div>
                  <div>
                    <span className="text-gray-400 block text-[10px] uppercase font-sans">Java Version</span>
                    <strong className="text-amber-300">Java {projectMeta.javaVersion || "21"}</strong>
                  </div>
                  <div>
                    <span className="text-gray-400 block text-[10px] uppercase font-sans">Build Tool</span>
                    <strong className="text-cyan-400">{projectMeta.buildTool || "Maven"}</strong>
                  </div>
                  <div>
                    <span className="text-gray-400 block text-[10px] uppercase font-sans">Parsed Files</span>
                    <strong>{projectMeta.totalJavaFiles || "30+"} Java Source Files</strong>
                  </div>
                </div>
              )}

              {/* Entities Grid */}
              <div className="p-4 bg-gray-50 rounded-2xl border border-gray-200 space-y-3">
                <h5 className="text-xs font-extrabold text-gray-900 flex items-center gap-1.5">
                  <Database className="w-4 h-4 text-purple-600" />
                  <span>Discovered JPA Domain Entities ({entities.length})</span>
                </h5>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {entities.length > 0 ? (
                    entities.map((ent: any, i: number) => (
                      <div key={i} className="p-3 bg-white rounded-xl border border-gray-200 space-y-2">
                        <div className="flex items-center justify-between">
                          <strong className="text-xs font-bold text-gray-900 font-mono">
                            {typeof ent === "string" ? ent : ent.className}
                          </strong>
                          {ent.tableName && (
                            <span className="text-[10px] font-mono text-gray-500 bg-gray-100 px-1.5 py-0.5 rounded">
                              table: {ent.tableName}
                            </span>
                          )}
                        </div>
                        {ent.fields && Array.isArray(ent.fields) && (
                          <div className="flex flex-wrap gap-1">
                            {ent.fields.map((f: any, fi: number) => (
                              <span
                                key={fi}
                                className={`text-[10px] font-mono px-1.5 py-0.5 rounded border ${
                                  f.isId ? "bg-amber-50 border-amber-200 text-amber-900 font-bold" : "bg-gray-50 border-gray-200 text-gray-700"
                                }`}
                              >
                                {f.name}: {f.type}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>
                    ))
                  ) : (
                    <div className="text-xs text-gray-500 font-mono">No entities detected</div>
                  )}
                </div>
              </div>

              {/* Controllers, Services & Repositories Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="p-4 bg-gray-50 rounded-2xl border border-gray-200 space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 flex items-center gap-1.5">
                    <Layers className="w-4 h-4 text-[#F05323]" />
                    Controllers ({controllers.length})
                  </h5>
                  <div className="space-y-1.5 font-mono text-[11px] text-gray-700">
                    {controllers.length > 0 ? (
                      controllers.map((c: any, i: number) => (
                        <div key={i} className="p-2 bg-white rounded-lg border border-gray-100">
                          <strong className="text-gray-900">{typeof c === "string" ? c : c.className}</strong>
                          {c.basePath && (
                            <span className="text-gray-500 block text-[10px]">
                              Base: /{c.basePath}
                            </span>
                          )}
                        </div>
                      ))
                    ) : (
                      <div className="text-gray-400">None detected</div>
                    )}
                  </div>
                </div>

                <div className="p-4 bg-gray-50 rounded-2xl border border-gray-200 space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 flex items-center gap-1.5">
                    <Sparkles className="w-4 h-4 text-emerald-600" />
                    Services ({services.length})
                  </h5>
                  <div className="space-y-1.5 font-mono text-[11px] text-gray-700">
                    {services.length > 0 ? (
                      services.map((s: any, i: number) => (
                        <div key={i} className="p-2 bg-white rounded-lg border border-gray-100">
                          <strong className="text-gray-900">{typeof s === "string" ? s : s.className}</strong>
                          {s.packageName && (
                            <span className="text-gray-500 block text-[10px] truncate">
                              {s.packageName}
                            </span>
                          )}
                        </div>
                      ))
                    ) : (
                      <div className="text-gray-400">None detected</div>
                    )}
                  </div>
                </div>

                <div className="p-4 bg-gray-50 rounded-2xl border border-gray-200 space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 flex items-center gap-1.5">
                    <FolderTree className="w-4 h-4 text-indigo-600" />
                    Repositories ({repositories.length})
                  </h5>
                  <div className="space-y-1.5 font-mono text-[11px] text-gray-700">
                    {repositories.length > 0 ? (
                      repositories.map((r: any, i: number) => (
                        <div key={i} className="p-2 bg-white rounded-lg border border-gray-100">
                          <strong className="text-gray-900">{typeof r === "string" ? r : r.interfaceName}</strong>
                          {r.domainEntity && (
                            <span className="text-gray-500 block text-[10px]">
                              Entity: {r.domainEntity}
                            </span>
                          )}
                        </div>
                      ))
                    ) : (
                      <div className="text-gray-400">None detected</div>
                    )}
                  </div>
                </div>
              </div>

              {/* Dependencies */}
              {dependencies.length > 0 && (
                <div className="p-4 bg-gray-50 rounded-2xl border border-gray-200 space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 flex items-center gap-1.5">
                    <Code2 className="w-4 h-4 text-blue-600" />
                    Starter Dependencies ({dependencies.length})
                  </h5>
                  <div className="flex flex-wrap gap-1.5 font-mono text-[10px]">
                    {dependencies.map((dep: string, i: number) => (
                      <span key={i} className="px-2 py-0.5 rounded bg-white border border-gray-200 text-gray-700">
                        {dep}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {activeTab === "ENDPOINTS" && (
            <div className="space-y-2.5">
              <h5 className="text-xs font-extrabold text-gray-900 uppercase tracking-wider pb-1">
                Discovered REST Endpoints in Codebase ({endpoints.length})
              </h5>
              <div className="space-y-2">
                {endpoints.length > 0 ? (
                  endpoints.map((ep: any, i: number) => (
                    <div
                      key={i}
                      className="p-3 bg-gray-50 rounded-xl border border-gray-200 flex items-center justify-between font-mono text-xs hover:border-[#F05323]/50 transition-colors"
                    >
                      <div className="flex items-center gap-2">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                          ep.method === "GET" ? "bg-blue-100 text-blue-800" :
                          ep.method === "POST" ? "bg-emerald-100 text-emerald-800" :
                          ep.method === "PUT" ? "bg-amber-100 text-amber-800" :
                          ep.method === "DELETE" ? "bg-rose-100 text-rose-800" :
                          "bg-purple-100 text-purple-800"
                        }`}>
                          {ep.method}
                        </span>
                        <span className="font-bold text-gray-900">{ep.path}</span>
                      </div>

                      <div className="text-right">
                        <span className="text-gray-600 text-[11px] block">{ep.controllerMethod}</span>
                        {ep.returnType && (
                          <span className="text-[10px] text-gray-400 block">{ep.returnType}</span>
                        )}
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="p-6 text-center text-xs text-gray-500 font-mono">
                    No explicit endpoints detected in scanned controllers.
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="pt-2 border-t border-gray-100 flex justify-end">
          <Button
            size="sm"
            onClick={onClose}
            className="bg-gray-900 hover:bg-black text-white text-xs font-bold"
          >
            Close Inspector
          </Button>
        </div>
      </div>
    </div>
  );
};
