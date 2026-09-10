import React from "react";

import { APP_NAME } from "../../lib/utils/constant";

const REPO_URL =
  "https://github.com/Cognition-Partner-Workshops/uc-spring-boot-upgrade-microservice-extraction";

const Footer = () => (
  <footer>
    <div className="container">
      <a href="/" className="logo-font">
        conduit
      </a>
      <span className="attribution">
        &copy; {new Date().getFullYear()} {APP_NAME} &middot; Spring Boot 2.6.3
        / Java 11 monolith &middot;{" "}
        <a href={REPO_URL} target="_blank" rel="noopener noreferrer">
          GitHub
        </a>
      </span>
    </div>
  </footer>
);

export default Footer;
