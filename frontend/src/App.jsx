import { useState, useEffect } from "react";
import { NavLink, Route, Routes, useNavigate, Navigate } from "react-router-dom";
import { getAuth, setAuth } from "./api";
import Login from "./pages/Login.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";
import ManageIssues from "./pages/ManageIssues.jsx";
import DisputeLedger from "./pages/DisputeLedger.jsx";
import RegistryAndCosts from "./pages/RegistryAndCosts.jsx";
import TenantPortal from "./pages/TenantPortal.jsx";
import VendorPortal from "./pages/VendorPortal.jsx";

export default function App() {
  const [auth, setAuthState] = useState(getAuth());
  const navigate = useNavigate();

  useEffect(() => {
    const handleAuthRequired = () => {
      setAuthState({ token: null, user: null });
      navigate("/login", { replace: true });
    };

    window.addEventListener("cap43_auth_required", handleAuthRequired);
    return () => window.removeEventListener("cap43_auth_required", handleAuthRequired);
  }, [navigate]);

  const handleLoginSuccess = () => {
    const updated = getAuth();
    setAuthState(updated);
    if (updated.user?.role === "ADMIN") {
      navigate("/admin");
    } else if (updated.user?.role === "VENDOR") {
      navigate("/vendor");
    } else {
      navigate("/tenant");
    }
  };

  const handleLogout = () => {
    setAuth(null, null);
    setAuthState({ token: null, user: null });
    navigate("/login");
  };

  const user = auth.user;
  const role = user?.role;

  return (
    <>
      <nav className="nav">
        <div className="nav-brand">
          <span className="brand-logo">🏢 PropLedger</span>
          <span className="brand-divider">|</span>
          <span className="brand-tagline">
            Maintenance & Dispute Ledger
          </span>
        </div>

        {user ? (
          <div className="nav-links">
            {role === "ADMIN" && (
              <>
                <NavLink to="/admin" end>Dashboard & Analytics</NavLink>
                <NavLink to="/admin/issues">Issues & SLAs</NavLink>
                <NavLink to="/admin/disputes">Disputes & Rules</NavLink>
                <NavLink to="/admin/registry">Registry & Costs</NavLink>
              </>
            )}

            {role === "TENANT" && (
              <NavLink to="/tenant">Tenant Portal</NavLink>
            )}

            {role === "VENDOR" && (
              <NavLink to="/vendor">Vendor Work Orders</NavLink>
            )}

            <div className="user-badge">
              <span className="user-avatar">👤</span>
              <span className="user-name">{user.fullName}</span>
              <span className={`role-pill role-${role ? role.toLowerCase() : "user"}`}>{role}</span>
              <button className="btn-logout" onClick={handleLogout} title="Sign Out">Sign Out</button>
            </div>
          </div>
        ) : (
          <div className="nav-links">
            <NavLink to="/login" end>Sign In</NavLink>
            <NavLink to="/register">Create Account</NavLink>
          </div>
        )}
      </nav>

      <main className="container">
        <Routes>
          <Route path="/login" element={<Login onLoginSuccess={handleLoginSuccess} initialMode="login" />} />
          <Route path="/register" element={<Login onLoginSuccess={handleLoginSuccess} initialMode="register" />} />

          <Route
            path="/admin"
            element={user && role === "ADMIN" ? <AdminDashboard /> : <Navigate to="/login" replace />}
          />
          <Route
            path="/admin/issues"
            element={user && role === "ADMIN" ? <ManageIssues /> : <Navigate to="/login" replace />}
          />
          <Route
            path="/admin/disputes"
            element={user && role === "ADMIN" ? <DisputeLedger /> : <Navigate to="/login" replace />}
          />
          <Route
            path="/admin/registry"
            element={user && role === "ADMIN" ? <RegistryAndCosts /> : <Navigate to="/login" replace />}
          />

          <Route
            path="/tenant"
            element={user && role === "TENANT" ? <TenantPortal /> : <Navigate to="/login" replace />}
          />

          <Route
            path="/vendor"
            element={user && role === "VENDOR" ? <VendorPortal /> : <Navigate to="/login" replace />}
          />

          <Route
            path="*"
            element={<Navigate to={user ? (role === "ADMIN" ? "/admin" : role === "VENDOR" ? "/vendor" : "/tenant") : "/login"} replace />}
          />
        </Routes>
      </main>
    </>
  );
}
