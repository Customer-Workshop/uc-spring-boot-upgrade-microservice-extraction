import React from "react";
import Link from "next/link";
import { useRouter } from "next/router";
import useSWR from "swr";

import Maybe from "./Maybe";
import checkLogin from "../../lib/utils/checkLogin";
import storage from "../../lib/utils/storage";
import { usePageDispatch } from "../../lib/context/PageContext";

/* Lucide-style SVG icons (inline to avoid ESM compat issues with Next.js 9) */
const IconHome = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
    <polyline points="9 22 9 12 15 12 15 22" />
  </svg>
);

const IconFolderKanban = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M4 20h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.93a2 2 0 0 1-1.66-.9l-.82-1.2A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13c0 1.1.9 2 2 2Z" />
    <path d="M8 10v4" /><path d="M12 10v2" /><path d="M16 10v6" />
  </svg>
);

const IconPenSquare = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
    <path d="M18.375 2.625a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4Z" />
  </svg>
);

const IconSettings = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z" />
    <circle cx="12" cy="12" r="3" />
  </svg>
);

const IconUser = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
    <circle cx="12" cy="7" r="4" />
  </svg>
);

const IconLogIn = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
    <polyline points="10 17 15 12 10 7" />
    <line x1="15" y1="12" x2="3" y2="12" />
  </svg>
);

const IconUserPlus = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
    <circle cx="9" cy="7" r="4" />
    <line x1="19" y1="8" x2="19" y2="14" />
    <line x1="22" y1="11" x2="16" y2="11" />
  </svg>
);

interface SidebarLinkProps {
  href: string;
  icon: React.ReactNode;
  label: string;
  active: boolean;
  onClick?: () => void;
}

const SidebarLink = ({ href, icon, label, active, onClick }: SidebarLinkProps) => (
  <Link href={href} passHref>
    <a
      onClick={onClick}
      style={{
        display: "flex",
        alignItems: "center",
        gap: "12px",
        padding: "10px 16px",
        borderRadius: "8px",
        fontSize: "14px",
        fontWeight: active ? 600 : 400,
        color: active ? "#ffffff" : "rgba(255,255,255,0.6)",
        backgroundColor: active ? "rgba(255,255,255,0.1)" : "transparent",
        textDecoration: "none",
        transition: "all 0.15s ease",
        marginBottom: "2px",
      }}
      onMouseEnter={(e) => {
        if (!active) {
          (e.currentTarget as HTMLElement).style.backgroundColor = "rgba(255,255,255,0.06)";
          (e.currentTarget as HTMLElement).style.color = "rgba(255,255,255,0.85)";
        }
      }}
      onMouseLeave={(e) => {
        if (!active) {
          (e.currentTarget as HTMLElement).style.backgroundColor = "transparent";
          (e.currentTarget as HTMLElement).style.color = "rgba(255,255,255,0.6)";
        }
      }}
    >
      {icon}
      <span>{label}</span>
    </a>
  </Link>
);

const Sidebar = () => {
  const setPage = usePageDispatch();
  const { data: currentUser } = useSWR("user", storage);
  const isLoggedIn = checkLogin(currentUser);
  const router = useRouter();
  const currentPath = router.asPath;

  const handleHomeClick = React.useCallback(() => setPage(0), []);

  return (
    <aside
      style={{
        width: "240px",
        minHeight: "100vh",
        backgroundColor: "#0a0a0a",
        display: "flex",
        flexDirection: "column",
        padding: "0",
        position: "fixed",
        top: 0,
        left: 0,
        zIndex: 50,
        borderRight: "1px solid rgba(255,255,255,0.08)",
        fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
      }}
    >
      {/* Logo */}
      <div
        style={{
          padding: "24px 20px 20px",
          borderBottom: "1px solid rgba(255,255,255,0.08)",
        }}
      >
        <Link href="/" passHref>
          <a
            onClick={handleHomeClick}
            style={{
              fontSize: "20px",
              fontWeight: 700,
              color: "#ffffff",
              textDecoration: "none",
              letterSpacing: "-0.02em",
            }}
          >
            conduit
          </a>
        </Link>
        <p
          style={{
            fontSize: "11px",
            color: "rgba(255,255,255,0.35)",
            marginTop: "4px",
            marginBottom: 0,
          }}
        >
          Project Management
        </p>
      </div>

      {/* Navigation */}
      <nav style={{ padding: "12px 12px", flex: 1 }}>
        <p
          style={{
            fontSize: "11px",
            fontWeight: 600,
            color: "rgba(255,255,255,0.3)",
            textTransform: "uppercase",
            letterSpacing: "0.05em",
            padding: "8px 16px 6px",
            marginBottom: "2px",
          }}
        >
          Menu
        </p>

        <SidebarLink
          href="/"
          icon={<IconHome />}
          label="Home"
          active={currentPath === "/"}
          onClick={handleHomeClick}
        />

        <Maybe test={isLoggedIn}>
          <SidebarLink
            href="/projects"
            icon={<IconFolderKanban />}
            label="Projects"
            active={currentPath === "/projects"}
          />
          <SidebarLink
            href="/editor/new"
            icon={<IconPenSquare />}
            label="New Post"
            active={currentPath === "/editor/new"}
          />

          <div style={{ marginTop: "24px" }}>
            <p
              style={{
                fontSize: "11px",
                fontWeight: 600,
                color: "rgba(255,255,255,0.3)",
                textTransform: "uppercase",
                letterSpacing: "0.05em",
                padding: "8px 16px 6px",
                marginBottom: "2px",
              }}
            >
              Account
            </p>
            <SidebarLink
              href="/user/settings"
              icon={<IconSettings />}
              label="Settings"
              active={currentPath === "/user/settings"}
            />
            <SidebarLink
              href={`/profile/${currentUser?.username}`}
              icon={<IconUser />}
              label={currentUser?.username || "Profile"}
              active={currentPath === `/profile/${currentUser?.username}`}
            />
          </div>
        </Maybe>

        <Maybe test={!isLoggedIn}>
          <SidebarLink
            href="/user/login"
            icon={<IconLogIn />}
            label="Sign in"
            active={currentPath === "/user/login"}
          />
          <SidebarLink
            href="/user/register"
            icon={<IconUserPlus />}
            label="Sign up"
            active={currentPath === "/user/register"}
          />
        </Maybe>
      </nav>

      {/* Footer */}
      <div
        style={{
          padding: "16px 20px",
          borderTop: "1px solid rgba(255,255,255,0.08)",
          fontSize: "11px",
          color: "rgba(255,255,255,0.25)",
        }}
      >
        Built with Next.js
      </div>
    </aside>
  );
};

export default Sidebar;
