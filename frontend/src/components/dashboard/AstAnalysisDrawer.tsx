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
import { FormattedMarkdown, cleanRawString } from "@/components/common/FormattedMarkdown";

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
  const normalizeSpecList = (val: any): string[] => {
    if (!val) return [];
    if (Array.isArray(val)) {
      const list: string[] = [];
      val.forEach((item) => {
        if (typeof item === "string") {
          const cleaned = cleanRawString(item);
          if (cleaned.includes("\n")) {
            cleaned.split("\n").forEach((s) => {
              const t = s.trim().replace(/^[-*•\d.]+\s*/, "");
              if (t) list.push(t);
            });
          } else {
            list.push(cleaned);
          }
        } else if (item && typeof item === "object") {
          const itemStr = item.requirement || item.item || item.description || item.text || item.constraint || JSON.stringify(item);
          list.push(cleanRawString(itemStr));
        } else {
          list.push(String(item));
        }
      });
      return list.filter(Boolean);
    }
    if (typeof val === "object") {
      if (Array.isArray(val.items)) return normalizeSpecList(val.items);
      if (Array.isArray(val.requirements)) return normalizeSpecList(val.requirements);
      if (Array.isArray(val.constraints)) return normalizeSpecList(val.constraints);
      return Object.values(val).map((v) => cleanRawString(typeof v === "string" ? v : JSON.stringify(v))).filter(Boolean);
    }
    if (typeof val === "string") {
      const cleaned = cleanRawString(val);
      try {
        const parsed = JSON.parse(cleaned);
        return normalizeSpecList(parsed);
      } catch {
        return cleaned
          .split("\n")
          .map((s) => s.trim().replace(/^[-*•\d.]+\s*/, ""))
          .filter(Boolean);
      }
    }
    return [cleanRawString(val)].filter(Boolean);
  };

  const specRequirements = normalizeSpecList(rawSpec?.requirements).length > 0
    ? normalizeSpecList(rawSpec?.requirements)
    : ["Implement the requested feature endpoint", "Ensure standard HTTP response status codes"];

  const specConstraints = normalizeSpecList(rawSpec?.constraints).length > 0
    ? normalizeSpecList(rawSpec?.constraints)
    : ["Validate input payloads", "Follow Spring Boot best practices"];

  const formatSpecObj = (val: any): string => {
    if (!val) return "";
    if (typeof val === "string") {
      const cleaned = cleanRawString(val);
      try {
        const parsed = JSON.parse(cleaned);
        return JSON.stringify(parsed, null, 2);
      } catch {
        return cleaned;
      }
    }
    if (typeof val === "object") {
      return JSON.stringify(val, null, 2);
    }
    return String(val);
  };

  const specRequestStr = formatSpecObj(rawSpec?.requestSpecification);
  const specResponseStr = formatSpecObj(rawSpec?.responseSpecification);

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
      <div className="bg-white dark:bg-slate-900 rounded-3xl max-w-4xl w-full p-6 sm:p-8 shadow-2xl border border-gray-100 dark:border-slate-800 space-y-5 animate-in zoom-in-95 duration-200 max-h-[90vh] flex flex-col transition-colors">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-gray-100 dark:border-slate-800">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-primary-light dark:bg-primary/25 text-primary dark:text-primary flex items-center justify-center font-bold">
              <Binary className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-lg font-extrabold text-gray-900 dark:text-white">
                  Codebase AST & AI Feature Specification
                </h3>
                <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-800 dark:text-emerald-300">
                  LIVE AST PARSER
                </span>
              </div>
              <p className="text-xs text-gray-500 dark:text-slate-400 font-mono">
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
              {copied ? <Check className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
              <span>{copied ? "Copied" : "Copy Complete JSON"}</span>
            </Button>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full bg-gray-100 dark:bg-slate-800 hover:bg-gray-200 dark:hover:bg-slate-700 flex items-center justify-center text-gray-500 dark:text-slate-400 transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Tab Buttons */}
        <div className="flex items-center gap-2 border-b border-gray-100 dark:border-slate-800 pb-2">
          <button
            onClick={() => setActiveTab("FEATURE")}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 cursor-pointer ${
              activeTab === "FEATURE"
                ? "bg-primary text-white shadow-xs"
                : "text-gray-600 dark:text-slate-400 hover:bg-gray-100 dark:hover:bg-slate-800"
            }`}
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>AI Feature Specification</span>
          </button>

          <button
            onClick={() => setActiveTab("AST_STRUCTURE")}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 cursor-pointer ${
              activeTab === "AST_STRUCTURE"
                ? "bg-primary text-white shadow-xs"
                : "text-gray-600 dark:text-slate-400 hover:bg-gray-100 dark:hover:bg-slate-800"
            }`}
          >
            <Layers className="w-3.5 h-3.5" />
            <span>Discovered AST Architecture</span>
          </button>

          <button
            onClick={() => setActiveTab("ENDPOINTS")}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5 cursor-pointer ${
              activeTab === "ENDPOINTS"
                ? "bg-primary text-white shadow-xs"
                : "text-gray-600 dark:text-slate-400 hover:bg-gray-100 dark:hover:bg-slate-800"
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
              <div className="p-4 bg-primary-light/50 dark:bg-primary/25/20 rounded-2xl border border-primary-border/80 dark:border-primary/30 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-bold uppercase tracking-wider text-primary dark:text-primary block">
                    AI Feature Specification
                  </span>
                  <div className="flex items-center gap-1.5 font-mono text-xs">
                    <span className="px-2 py-0.5 rounded bg-primary text-white font-bold text-[10px]">
                      {specHttpMethod}
                    </span>
                    <span className="text-gray-900 dark:text-white font-bold">{specEndpoint}</span>
                  </div>
                </div>
                <h4 className="text-base font-extrabold text-gray-900 dark:text-white">
                  {specTitle}
                </h4>
                <FormattedMarkdown
                  content={specDescription}
                  theme="light"
                  className="text-xs text-gray-700 dark:text-slate-300 leading-relaxed"
                />
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
                  <div className="text-gray-300 pt-2 text-[11px] bg-black/40 p-2.5 rounded-xl space-y-1">
                    <strong className="text-emerald-400 block font-sans text-[10px] uppercase">Request Specification:</strong>
                    <FormattedMarkdown content={specRequestStr} theme="dark" />
                  </div>
                )}
                {specResponseStr && (
                  <div className="text-gray-300 pt-1 text-[11px] bg-black/40 p-2.5 rounded-xl space-y-1">
                    <strong className="text-cyan-400 block font-sans text-[10px] uppercase">Expected Response Specification:</strong>
                    <FormattedMarkdown content={specResponseStr} theme="dark" />
                  </div>
                )}
              </div>

              {/* Requirements Checklist */}
              {specRequirements.length > 0 && (
                <div className="space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 dark:text-white uppercase tracking-wider">
                    Functional Requirements
                  </h5>
                  <ul className="space-y-1.5">
                    {specRequirements.map((req: string, i: number) => (
                      <li key={i} className="flex items-start gap-2.5 text-xs text-gray-700 dark:text-slate-300 bg-gray-50/80 dark:bg-slate-800/80 p-2.5 rounded-xl border border-gray-100 dark:border-slate-700">
                        <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400 shrink-0 mt-0.5" />
                        <div className="flex-1">
                          <FormattedMarkdown content={req} theme="light" />
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Constraints */}
              {specConstraints.length > 0 && (
                <div className="space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 dark:text-white uppercase tracking-wider">
                    Architectural Constraints
                  </h5>
                  <ul className="space-y-1.5">
                    {specConstraints.map((c: string, i: number) => (
                      <li key={i} className="flex items-start gap-2.5 text-xs text-gray-600 dark:text-slate-400 bg-gray-50/80 dark:bg-slate-800/80 p-2.5 rounded-xl border border-gray-100 dark:border-slate-700">
                        <ChevronRight className="w-4 h-4 text-primary shrink-0 mt-0.5" />
                        <div className="flex-1">
                          <FormattedMarkdown content={c} theme="light" />
                        </div>
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
              <div className="p-4 bg-gray-50 dark:bg-slate-800/50 rounded-2xl border border-gray-200 dark:border-slate-700 space-y-3">
                <h5 className="text-xs font-extrabold text-gray-900 dark:text-white flex items-center gap-1.5">
                  <Database className="w-4 h-4 text-purple-600 dark:text-purple-400" />
                  <span>Discovered JPA Domain Entities ({entities.length})</span>
                </h5>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {entities.length > 0 ? (
                    entities.map((ent: any, i: number) => (
                      <div key={i} className="p-3 bg-white dark:bg-slate-900 rounded-xl border border-gray-200 dark:border-slate-700 space-y-2">
                        <div className="flex items-center justify-between">
                          <strong className="text-xs font-bold text-gray-900 dark:text-white font-mono">
                            {typeof ent === "string" ? ent : ent.className}
                          </strong>
                          {ent.tableName && (
                            <span className="text-[10px] font-mono text-gray-500 dark:text-slate-400 bg-gray-100 dark:bg-slate-800 px-1.5 py-0.5 rounded">
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
                                  f.isId ? "bg-amber-50 dark:bg-amber-950/60 border-amber-200 dark:border-amber-800 text-amber-900 dark:text-amber-300 font-bold" : "bg-gray-50 dark:bg-slate-800 border-gray-200 dark:border-slate-700 text-gray-700 dark:text-slate-300"
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
                    <div className="text-xs text-gray-500 dark:text-slate-400 font-mono">No entities detected</div>
                  )}
                </div>
              </div>

              {/* Controllers, Services & Repositories Grid */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="p-4 bg-gray-50 dark:bg-slate-800/50 rounded-2xl border border-gray-200 dark:border-slate-700 space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 dark:text-white flex items-center gap-1.5">
                    <Layers className="w-4 h-4 text-primary dark:text-primary" />
                    Controllers ({controllers.length})
                  </h5>
                  <div className="space-y-1.5 font-mono text-[11px] text-gray-700 dark:text-slate-300">
                    {controllers.length > 0 ? (
                      controllers.map((c: any, i: number) => (
                        <div key={i} className="p-2 bg-white dark:bg-slate-900 rounded-lg border border-gray-100 dark:border-slate-800">
                          <strong className="text-gray-900 dark:text-white">{typeof c === "string" ? c : c.className}</strong>
                          {c.basePath && (
                            <span className="text-gray-500 dark:text-slate-400 block text-[10px]">
                              Base: /{c.basePath}
                            </span>
                          )}
                        </div>
                      ))
                    ) : (
                      <div className="text-gray-400 dark:text-slate-500">None detected</div>
                    )}
                  </div>
                </div>

                <div className="p-4 bg-gray-50 dark:bg-slate-800/50 rounded-2xl border border-gray-200 dark:border-slate-700 space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 dark:text-white flex items-center gap-1.5">
                    <Sparkles className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                    Services ({services.length})
                  </h5>
                  <div className="space-y-1.5 font-mono text-[11px] text-gray-700 dark:text-slate-300">
                    {services.length > 0 ? (
                      services.map((s: any, i: number) => (
                        <div key={i} className="p-2 bg-white dark:bg-slate-900 rounded-lg border border-gray-100 dark:border-slate-800">
                          <strong className="text-gray-900 dark:text-white">{typeof s === "string" ? s : s.className}</strong>
                          {s.packageName && (
                            <span className="text-gray-500 dark:text-slate-400 block text-[10px] truncate">
                              {s.packageName}
                            </span>
                          )}
                        </div>
                      ))
                    ) : (
                      <div className="text-gray-400 dark:text-slate-500">None detected</div>
                    )}
                  </div>
                </div>

                <div className="p-4 bg-gray-50 dark:bg-slate-800/50 rounded-2xl border border-gray-200 dark:border-slate-700 space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 dark:text-white flex items-center gap-1.5">
                    <FolderTree className="w-4 h-4 text-indigo-600 dark:text-indigo-400" />
                    Repositories ({repositories.length})
                  </h5>
                  <div className="space-y-1.5 font-mono text-[11px] text-gray-700 dark:text-slate-300">
                    {repositories.length > 0 ? (
                      repositories.map((r: any, i: number) => (
                        <div key={i} className="p-2 bg-white dark:bg-slate-900 rounded-lg border border-gray-100 dark:border-slate-800">
                          <strong className="text-gray-900 dark:text-white">{typeof r === "string" ? r : r.interfaceName}</strong>
                          {r.domainEntity && (
                            <span className="text-gray-500 dark:text-slate-400 block text-[10px]">
                              Entity: {r.domainEntity}
                            </span>
                          )}
                        </div>
                      ))
                    ) : (
                      <div className="text-gray-400 dark:text-slate-500">None detected</div>
                    )}
                  </div>
                </div>
              </div>

              {/* Dependencies */}
              {dependencies.length > 0 && (
                <div className="p-4 bg-gray-50 dark:bg-slate-800/50 rounded-2xl border border-gray-200 dark:border-slate-700 space-y-2">
                  <h5 className="text-xs font-extrabold text-gray-900 dark:text-white flex items-center gap-1.5">
                    <Code2 className="w-4 h-4 text-blue-600 dark:text-blue-400" />
                    Starter Dependencies ({dependencies.length})
                  </h5>
                  <div className="flex flex-wrap gap-1.5 font-mono text-[10px]">
                    {dependencies.map((dep: string, i: number) => (
                      <span key={i} className="px-2 py-0.5 rounded bg-white dark:bg-slate-900 border border-gray-200 dark:border-slate-700 text-gray-700 dark:text-slate-300">
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
              <h5 className="text-xs font-extrabold text-gray-900 dark:text-white uppercase tracking-wider pb-1">
                Discovered REST Endpoints in Codebase ({endpoints.length})
              </h5>
              <div className="space-y-2">
                {endpoints.length > 0 ? (
                  endpoints.map((ep: any, i: number) => (
                    <div
                      key={i}
                      className="p-3 bg-gray-50 dark:bg-slate-800/60 rounded-xl border border-gray-200 dark:border-slate-700 flex items-center justify-between font-mono text-xs hover:border-primary/50 transition-colors"
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
                        <span className="font-bold text-gray-900 dark:text-white">{ep.path}</span>
                      </div>

                      <div className="text-right">
                        <span className="text-gray-600 dark:text-slate-300 text-[11px] block">{ep.controllerMethod}</span>
                        {ep.returnType && (
                          <span className="text-[10px] text-gray-400 dark:text-slate-500 block">{ep.returnType}</span>
                        )}
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="p-6 text-center text-xs text-gray-500 dark:text-slate-400 font-mono">
                    No explicit endpoints detected in scanned controllers.
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="pt-2 border-t border-gray-100 dark:border-slate-800 flex justify-end">
          <Button
            size="sm"
            onClick={onClose}
            className="bg-gray-900 hover:bg-black dark:bg-slate-800 dark:hover:bg-slate-700 text-white text-xs font-bold"
          >
            Close Inspector
          </Button>
        </div>
      </div>
    </div>
  );
};
