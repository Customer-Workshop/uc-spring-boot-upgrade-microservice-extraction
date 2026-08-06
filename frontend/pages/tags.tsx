import Head from "next/head";
import React from "react";
import useSWR from "swr";

import ErrorMessage from "../components/common/ErrorMessage";
import LoadingSpinner from "../components/common/LoadingSpinner";
import TagStatsList from "../components/tags/TagStatsList";
import { TagStatsResponse } from "../lib/types/tagType";
import { SERVER_BASE_URL } from "../lib/utils/constant";
import fetcher from "../lib/utils/fetcher";

const Tags = () => {
  const { data, error } = useSWR<TagStatsResponse>(
    `${SERVER_BASE_URL}/tags/stats`,
    fetcher
  );

  return (
    <>
      <Head>
        <title>POPULAR TAGS | NEXT REALWORLD</title>
        <meta
          name="description"
          content="Browse tags ranked by the number of published articles."
        />
      </Head>
      <div className="tags-page">
        <div className="container page">
          <div className="row">
            <div className="col-md-9 offset-md-1 col-xs-12">
              <h1>Popular Tags</h1>
              {error ? (
                <ErrorMessage message="Cannot load popular tags..." />
              ) : !data ? (
                <LoadingSpinner />
              ) : (
                <TagStatsList tags={data.tags} />
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default Tags;
