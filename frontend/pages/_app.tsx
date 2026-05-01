import Head from "next/head";
import React from "react";
import { SWRConfig } from "swr";

import Layout from "components/common/Layout";
import ContextProvider from "lib/context";
import "styles.css";

if (typeof window !== "undefined") {
  require("lazysizes/plugins/attrchange/ls.attrchange.js");
  require("lazysizes/plugins/respimg/ls.respimg.js");
  require("lazysizes");
}

const MyApp = ({ Component, pageProps }) => (
  <>
    <Head>
      <meta
        name="viewport"
        content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"
      />
    </Head>
    <SWRConfig
      value={{
        dedupingInterval: 5000,
        revalidateOnFocus: false,
        shouldRetryOnError: false,
      }}
    >
      <ContextProvider>
        <Layout>
          <Component {...pageProps} />
        </Layout>
      </ContextProvider>
    </SWRConfig>
  </>
);

export default MyApp;
