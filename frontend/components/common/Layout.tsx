import React from "react";

import Sidebar from "./Sidebar";

const Layout = ({ children }) => (
  <div style={{ display: "flex", minHeight: "100vh" }}>
    <Sidebar />
    <main
      style={{
        flex: 1,
        marginLeft: "240px",
        backgroundColor: "#fefbf4",
        minHeight: "100vh",
      }}
    >
      {children}
    </main>
  </div>
);

export default Layout;
