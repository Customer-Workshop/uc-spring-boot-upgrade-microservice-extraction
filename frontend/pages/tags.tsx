import Head from "next/head";
import React from "react";
import useSWR from "swr";

import ErrorMessage from "../components/common/ErrorMessage";
import LoadingSpinner from "../components/common/LoadingSpinner";
import TagStats from "../components/tags/TagStats";
import { TagStatsList } from "../lib/types/tagType";
import fetcher from "../lib/utils/fetcher";
import { SERVER_BASE_URL } from "../lib/utils/constant";

const TagsPage = () => {
  const { data, error } = useSWR<TagStatsList>(
    `${SERVER_BASE_URL}/tags/stats`,
    fetcher
  );

  return (
    <>
      <Head>
        <title>POPULAR TAGS | NEXT REALWORLD</title>
        <meta
          name="description"
          content="Browse the most popular tags and the articles associated with them."
        />
      </Head>
      <div className="tags-page">
        <div className="banner">
          <div className="container">
            <h1 className="logo-font">Popular Tags</h1>
            <p>Explore articles by topic.</p>
          </div>
        </div>
        <div className="container page">
          {error ? (
            <ErrorMessage message="Cannot load popular tags..." />
          ) : !data ? (
            <LoadingSpinner />
          ) : data.tags.length === 0 ? (
            <div className="article-preview">No tags are here... yet.</div>
          ) : (
            <TagStats tags={data.tags} />
          )}
        </div>
      </div>
    </>
  );
};

export default TagsPage;
