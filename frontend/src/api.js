const BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

let authToken = localStorage.getItem("cap43_token") || null;
let currentUser = JSON.parse(localStorage.getItem("cap43_user") || "null");

export function setAuth(token, user) {
  authToken = token;
  currentUser = user;
  if (token) {
    localStorage.setItem("cap43_token", token);
    localStorage.setItem("cap43_user", JSON.stringify(user));
  } else {
    localStorage.removeItem("cap43_token");
    localStorage.removeItem("cap43_user");
  }
}

export function getAuth() {
  return { token: authToken, user: currentUser };
}

async function request(path, options = {}) {
  const headers = { ...options.headers };
  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }
  if (authToken) {
    headers["Authorization"] = `Bearer ${authToken}`;
  }

  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers,
  });

  if (!res.ok) {
    if (
      (res.status === 401 || res.status === 403) &&
      !path.startsWith("/api/auth/login") &&
      !path.startsWith("/api/auth/register")
    ) {
      setAuth(null, null);
      window.dispatchEvent(new CustomEvent("cap43_auth_required"));
    }

    let msg = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body.message) msg = body.message;
      else if (body.error) msg = `${body.error}: ${body.message || res.statusText}`;
    } catch {
    }
    throw new Error(msg);
  }

  if (res.status === 204) return null;
  return res.json();
}

export const login = (email, password) =>
  request("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });

export const register = (data) =>
  request("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(data),
  });

export const getMe = () => request("/api/auth/me");

export const getTenantIssues = () => request("/api/tenant/issues");
export const getTenantIssue = (id) => request(`/api/tenant/issues/${id}`);
export const createTenantIssue = (data) =>
  request("/api/tenant/issues", { method: "POST", body: JSON.stringify(data) });
export const uploadTenantEvidence = (issueId, formData) =>
  request(`/api/tenant/issues/${issueId}/evidence`, { method: "POST", body: formData });
export const getTenantDisputes = () => request("/api/tenant/disputes");
export const getTenantDispute = (id) => request(`/api/tenant/disputes/${id}`);
export const contestDisputeItem = (disputeId, itemId, data) =>
  request(`/api/tenant/disputes/${disputeId}/items/${itemId}/contest`, {
    method: "POST",
    body: JSON.stringify(data),
  });

export const getVendorWorkOrders = () => request("/api/vendor/work-orders");
export const updateVendorWorkOrder = (id, data) =>
  request(`/api/vendor/work-orders/${id}/updates`, {
    method: "POST",
    body: JSON.stringify(data),
  });

export const getDashboardSummary = () => request("/api/admin/dashboard/summary");
export const getEscalations = () => request("/api/admin/escalations");
export const acknowledgeEscalation = (id) =>
  request(`/api/admin/escalations/${id}/acknowledge`, { method: "POST" });
export const getProperties = () => request("/api/admin/properties");
export const createProperty = (data) =>
  request("/api/admin/properties", { method: "POST", body: JSON.stringify(data) });
export const getUnits = (propertyId) => request(`/api/admin/properties/${propertyId}/units`);
export const createUnit = (propertyId, data) =>
  request(`/api/admin/properties/${propertyId}/units`, { method: "POST", body: JSON.stringify(data) });
export const getLeases = (status) =>
  request(`/api/admin/leases${status ? `?status=${status}` : ""}`);
export const endLease = (leaseId, data) =>
  request(`/api/admin/leases/${leaseId}/end`, { method: "POST", body: JSON.stringify(data) });
export const getVendors = () => request("/api/admin/vendors");
export const getTenantsAdmin = () => request("/api/admin/tenants");
export const unmaskTenant = (tenantId, reason) =>
  request(`/api/admin/tenants/${tenantId}/unmask`, {
    method: "POST",
    body: JSON.stringify({ reason }),
  });

export const getAdminIssues = (params = {}) => {
  const query = new URLSearchParams();
  if (params.status) query.set("status", params.status);
  if (params.severity) query.set("severity", params.severity);
  if (params.overdue) query.set("overdue", "true");
  const qStr = query.toString();
  return request(`/api/admin/issues${qStr ? `?${qStr}` : ""}`);
};
export const getAdminIssueDetail = (id) => request(`/api/admin/issues/${id}`);
export const changeIssueSeverity = (id, data) =>
  request(`/api/admin/issues/${id}/severity`, { method: "PATCH", body: JSON.stringify(data) });
export const assignVendor = (id, data) =>
  request(`/api/admin/issues/${id}/work-orders`, { method: "POST", body: JSON.stringify(data) });

export const getLeaseInspections = (leaseId) => request(`/api/admin/leases/${leaseId}/inspections`);
export const getInspection = (id) => request(`/api/admin/inspections/${id}`);
export const createInspection = (leaseId, data) =>
  request(`/api/admin/leases/${leaseId}/inspections`, { method: "POST", body: JSON.stringify(data) });
export const addInspectionItem = (inspectionId, data) =>
  request(`/api/admin/inspections/${inspectionId}/items`, { method: "POST", body: JSON.stringify(data) });

export const getRepairCosts = (params = {}) => {
  const query = new URLSearchParams();
  if (params.issueId) query.set("issueId", params.issueId);
  if (params.workOrderId) query.set("workOrderId", params.workOrderId);
  if (params.inspectionItemId) query.set("inspectionItemId", params.inspectionItemId);
  const qStr = query.toString();
  return request(`/api/admin/repair-costs${qStr ? `?${qStr}` : ""}`);
};
export const createRepairCost = (data) =>
  request("/api/admin/repair-costs", { method: "POST", body: JSON.stringify(data) });
export const reverseRepairCost = (id, data) =>
  request(`/api/admin/repair-costs/${id}/reverse`, { method: "POST", body: JSON.stringify(data) });

export const getAdminDisputes = (status) =>
  request(`/api/admin/disputes${status ? `?status=${status}` : ""}`);
export const getAdminDispute = (id) => request(`/api/admin/disputes/${id}`);
export const createAdminDispute = (data) =>
  request("/api/admin/disputes", { method: "POST", body: JSON.stringify(data) });
export const decideDisputeItem = (disputeId, itemId, data) =>
  request(`/api/admin/disputes/${disputeId}/items/${itemId}/decide`, {
    method: "POST",
    body: JSON.stringify(data),
  });
export const getReviewQueue = (status) =>
  request(`/api/admin/disputes/review-queue${status ? `?status=${status}` : ""}`);
export const resolveReviewQueueItem = (id, data) =>
  request(`/api/admin/disputes/review-queue/${id}/resolve`, {
    method: "POST",
    body: JSON.stringify(data),
  });
export const getWearDamageRules = () => request("/api/admin/disputes/rules/wear-damage");
export const getSeverityRules = () => request("/api/admin/rules/severity");

export const triggerS3Sync = () =>
  request("/api/admin/sync/run", { method: "POST" });
