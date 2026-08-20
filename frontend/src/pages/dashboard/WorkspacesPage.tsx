import { useState } from "react";
import { Link } from "react-router-dom";
import { useHRStore } from "@/store/hrStore";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Folder,
  FolderPlus,
  Search,
  Users,
  FileCheck2,
  Clock,
  ArrowRight,
  Sparkles,
  X,
  Loader2,
  CheckCircle2,
  Calendar,
  Grid,
  List as ListIcon,
} from "lucide-react";

export const WorkspacesPage = () => {
  const { workspaces, createWorkspace } = useHRStore();
  const [searchQuery, setSearchQuery] = useState("");
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  // Form State
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [track, setTrack] = useState("Java Spring Boot Backend");
  const [durationMinutes, setDurationMinutes] = useState(90);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const filteredWorkspaces = workspaces.filter(
    (ws) =>
      ws.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      ws.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
      ws.track.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (!name.trim()) {
      setErrorMessage("Workspace name is required.");
      return;
    }

    setIsSubmitting(true);

    try {
      // Simulate network request
      await new Promise((resolve) => setTimeout(resolve, 400));
      const newWs = createWorkspace({
        name,
        description,
        track,
        defaultDurationMinutes: Number(durationMinutes),
      });

      setSuccessMessage(`Workspace "${newWs.name}" created successfully!`);
      setName("");
      setDescription("");
      setTimeout(() => {
        setIsCreateModalOpen(false);
        setSuccessMessage("");
      }, 1000);
    } catch {
      setErrorMessage("Failed to create workspace. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* 1. Header & Controls Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-gray-200/90 shadow-2xs">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-orange-50 text-[#F05323] border border-orange-200/60 mb-1.5">
            <Folder className="w-3 h-3 text-[#F05323]" />
            Workspace Directory
          </div>
          <h1 className="text-2xl font-extrabold text-gray-900 tracking-tight">
            Workspaces
          </h1>
          <p className="text-xs text-gray-500 font-medium">
            Manage placement drives, campus batches, and candidate evaluation hubs.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            size="default"
            onClick={() => setIsCreateModalOpen(true)}
            className="font-semibold gap-2 shadow-xs bg-[#F05323] hover:bg-[#d94417] text-white"
          >
            <FolderPlus className="w-4 h-4" />
            Create Workspace
          </Button>
        </div>
      </div>

      {/* 2. Search & View Mode Filter */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 bg-white px-4 py-3 rounded-2xl border border-gray-200 shadow-2xs">
        <div className="relative flex-1 w-full sm:max-w-md">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" />
          <Input
            type="text"
            placeholder="Search workspaces by name, college, or track..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9 text-xs h-9 bg-gray-50/70 border-gray-200"
          />
        </div>

        <div className="flex items-center gap-2 self-end sm:self-center">
          <span className="text-xs text-gray-500 font-medium">
            Showing <strong className="text-gray-900">{filteredWorkspaces.length}</strong> workspaces
          </span>

          <div className="h-4 w-px bg-gray-200 mx-1"></div>

          <div className="flex bg-gray-100 p-0.5 rounded-lg border border-gray-200">
            <button
              onClick={() => setViewMode("grid")}
              className={`p-1.5 rounded-md transition-colors ${
                viewMode === "grid"
                  ? "bg-white text-gray-900 shadow-2xs font-semibold"
                  : "text-gray-500 hover:text-gray-900"
              }`}
              title="Grid View"
            >
              <Grid className="w-3.5 h-3.5" />
            </button>
            <button
              onClick={() => setViewMode("list")}
              className={`p-1.5 rounded-md transition-colors ${
                viewMode === "list"
                  ? "bg-white text-gray-900 shadow-2xs font-semibold"
                  : "text-gray-500 hover:text-gray-900"
              }`}
              title="List View"
            >
              <ListIcon className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>

      {/* 3. Folder Directory / Workspace Grid */}
      {filteredWorkspaces.length === 0 ? (
        <div className="bg-white rounded-3xl border border-gray-200 p-12 text-center space-y-4">
          <div className="w-14 h-14 rounded-2xl bg-orange-50 text-[#F05323] flex items-center justify-center mx-auto">
            <FolderPlus className="w-7 h-7" />
          </div>
          <div>
            <h3 className="text-base font-bold text-gray-900">No workspaces found</h3>
            <p className="text-xs text-gray-500 max-w-sm mx-auto mt-1">
              {searchQuery
                ? `No workspaces matched "${searchQuery}". Try a different keyword.`
                : "Get started by creating your first recruitment workspace."}
            </p>
          </div>
          <Button
            size="sm"
            onClick={() => setIsCreateModalOpen(true)}
            className="gap-2 font-semibold bg-[#F05323] hover:bg-[#d94417]"
          >
            <FolderPlus className="w-4 h-4" /> Create Workspace
          </Button>
        </div>
      ) : viewMode === "grid" ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredWorkspaces.map((ws) => (
            <Link
              key={ws.id}
              to={`/dashboard/workspaces/${ws.id}`}
              className="group bg-white rounded-3xl border border-gray-200/90 p-6 shadow-2xs hover:shadow-md hover:border-orange-300 transition-all flex flex-col justify-between"
            >
              <div className="space-y-4">
                {/* Folder Top Tab */}
                <div className="flex items-center justify-between">
                  <div className="w-12 h-12 rounded-2xl bg-orange-50 border border-orange-100 flex items-center justify-center text-[#F05323] group-hover:scale-105 transition-transform">
                    <Folder className="w-6 h-6 fill-orange-100/60" />
                  </div>
                  <span className="inline-flex items-center gap-1 text-[11px] font-bold px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                    {ws.status}
                  </span>
                </div>

                {/* Workspace Title & Info */}
                <div>
                  <h3 className="text-lg font-bold text-gray-900 group-hover:text-[#F05323] transition-colors leading-tight">
                    {ws.name}
                  </h3>
                  <p className="text-xs text-gray-500 mt-1 line-clamp-2 leading-relaxed">
                    {ws.description || "Recruitment and technical assessment workspace"}
                  </p>
                </div>

                {/* Track Badge */}
                <div className="inline-flex items-center gap-1 text-[11px] font-semibold text-gray-600 bg-gray-100/80 px-2.5 py-1 rounded-lg border border-gray-200/60">
                  <Sparkles className="w-3 h-3 text-[#F05323]" />
                  <span>{ws.track}</span>
                </div>
              </div>

              {/* Stats Footer */}
              <div className="mt-6 pt-4 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500 font-medium">
                <div className="flex items-center gap-3">
                  <span className="flex items-center gap-1 text-gray-700">
                    <Users className="w-3.5 h-3.5 text-blue-600" />
                    <strong>{ws.candidateIds.length}</strong> Candidates
                  </span>
                  <span className="flex items-center gap-1 text-gray-700">
                    <FileCheck2 className="w-3.5 h-3.5 text-purple-600" />
                    <strong>{ws.candidateIds.length > 0 ? ws.candidateIds.length : 0}</strong> Assessments
                  </span>
                </div>
                <ArrowRight className="w-4 h-4 text-gray-400 group-hover:text-[#F05323] group-hover:translate-x-0.5 transition-all" />
              </div>
            </Link>
          ))}
        </div>
      ) : (
        /* List View */
        <div className="bg-white rounded-3xl border border-gray-200/90 shadow-2xs overflow-hidden divide-y divide-gray-100">
          {filteredWorkspaces.map((ws) => (
            <Link
              key={ws.id}
              to={`/dashboard/workspaces/${ws.id}`}
              className="flex items-center justify-between p-5 hover:bg-orange-50/30 transition-colors group"
            >
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-orange-50 border border-orange-100 flex items-center justify-center text-[#F05323] flex-shrink-0">
                  <Folder className="w-5 h-5 fill-orange-100/60" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-gray-900 group-hover:text-[#F05323] transition-colors">
                    {ws.name}
                  </h3>
                  <p className="text-xs text-gray-500 line-clamp-1">{ws.description}</p>
                </div>
              </div>

              <div className="flex items-center gap-6">
                <span className="text-xs font-semibold text-gray-600 bg-gray-100 px-2.5 py-1 rounded-lg hidden sm:block">
                  {ws.track}
                </span>

                <div className="flex items-center gap-4 text-xs text-gray-600">
                  <span className="flex items-center gap-1">
                    <Users className="w-3.5 h-3.5 text-blue-600" />
                    <strong>{ws.candidateIds.length}</strong>
                  </span>
                  <span className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5 text-gray-400" />
                    <span>{ws.createdAt}</span>
                  </span>
                </div>

                <ArrowRight className="w-4 h-4 text-gray-400 group-hover:text-[#F05323] group-hover:translate-x-0.5 transition-all" />
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* 4. Create Workspace Popup / Modal */}
      {isCreateModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 sm:p-8 shadow-2xl border border-gray-100 space-y-5 animate-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between pb-3 border-b border-gray-100">
              <div className="flex items-center gap-2.5">
                <div className="w-10 h-10 rounded-xl bg-orange-50 text-[#F05323] flex items-center justify-center">
                  <FolderPlus className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-lg font-extrabold text-gray-900">
                    Create New Workspace
                  </h3>
                  <p className="text-xs text-gray-500">
                    Set up a recruitment batch or placement drive workspace.
                  </p>
                </div>
              </div>

              <button
                onClick={() => setIsCreateModalOpen(false)}
                className="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center text-gray-500 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Error or Success Alert */}
            {errorMessage && (
              <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700 font-medium">
                {errorMessage}
              </div>
            )}
            {successMessage && (
              <div className="p-3 rounded-xl bg-emerald-50 border border-emerald-200 text-xs text-emerald-700 font-medium flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                <span>{successMessage}</span>
              </div>
            )}

            {/* Form */}
            <form onSubmit={handleCreateSubmit} className="space-y-4">
              {/* Workspace Name */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-gray-700 block">
                  Workspace Name <span className="text-[#F05323]">*</span>
                </label>
                <Input
                  type="text"
                  required
                  placeholder="e.g. IIT Bombay, NIT Trichy, TCS Placement"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="text-xs"
                />
              </div>

              {/* Description */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-gray-700 block">
                  Description
                </label>
                <textarea
                  rows={3}
                  placeholder="e.g. Campus placement drive for 2026 CS graduate backend developers..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="flex w-full rounded-xl border border-gray-300 bg-white px-3 py-2 text-xs text-gray-900 shadow-2xs focus:outline-none focus:ring-2 focus:ring-[#F05323] focus:border-[#F05323]"
                />
              </div>

              {/* Assessment Track / Role */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-gray-700 block">
                  Assessment Track / Role
                </label>
                <select
                  value={track}
                  onChange={(e) => setTrack(e.target.value)}
                  className="flex h-10 w-full rounded-xl border border-gray-300 bg-white px-3 text-xs text-gray-900 shadow-2xs focus:outline-none focus:ring-2 focus:ring-[#F05323] focus:border-[#F05323]"
                >
                  <option value="Java Spring Boot Backend">Java Spring Boot Backend</option>
                  <option value="Java Microservices & REST APIs">Java Microservices & REST APIs</option>
                  <option value="Fullstack Java & React">Fullstack Java & React</option>
                  <option value="Core Platform & Cloud Services">Core Platform & Cloud Services</option>
                </select>
              </div>

              {/* Default Duration */}
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-gray-700 block">
                  Default Assessment Duration (Minutes)
                </label>
                <div className="relative">
                  <Clock className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                  <Input
                    type="number"
                    min={30}
                    max={240}
                    step={15}
                    value={durationMinutes}
                    onChange={(e) => setDurationMinutes(Number(e.target.value))}
                    className="pl-9 text-xs"
                  />
                </div>
              </div>

              {/* Modal Buttons */}
              <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setIsCreateModalOpen(false)}
                  className="text-xs font-semibold"
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  disabled={isSubmitting}
                  size="sm"
                  className="text-xs font-semibold gap-2 bg-[#F05323] hover:bg-[#d94417] text-white shadow-xs"
                >
                  {isSubmitting ? (
                    <>
                      <Loader2 className="w-3.5 h-3.5 animate-spin" /> Creating...
                    </>
                  ) : (
                    <>
                      <FolderPlus className="w-3.5 h-3.5" /> Create Workspace
                    </>
                  )}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
