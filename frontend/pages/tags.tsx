import Head from "next/head";
import React from "react";
import useSWR from "swr";

import ErrorMessage from "../components/common/ErrorMessage";
import LoadingSpinner from "../components/common/LoadingSpinner";
import PopularTags from "../components/tags/PopularTags";
import { TagStatsResponse } from "../lib/types/tagType";
import { SERVER_BASE_URL } from "../lib/utils/constant";
import fetcher from "../lib/utils/fetcher";

const Tags = () => {
  const { data, error } = useSWR(
    `${SERVER_BASE_URL}/tags/stats`,
    fetcher
  );

  if (error) return <ErrorMessage message="Cannot load popular tags..." />;
  if (!data) return <LoadingSpinner />;

  const { tags } = data as TagStatsResponse;

  return (
    <>
      <Head>
        <title>POPULAR TAGS | NEXT REALWORLD</title>
        <meta
          name="description"
          content="Browse the most popular tags and articles on Conduit"
        />
      </Head>
      <div className="container page">
        <div className="row">
          <div className="col-xs-12 col-md-10 offset-md-1">
            <h1>Popular Tags</h1>
            {tags.length === 0 ? (
              <p>No popular tags yet.</p>
            ) : (
              <PopularTags tags={tags} />
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default Tags;
