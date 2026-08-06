import React from "react";

import CustomLink from "../common/CustomLink";
import { usePageDispatch } from "../../lib/context/PageContext";
import { TagStats } from "../../lib/types/tagType";

interface TagStatsListProps {
  tags: TagStats[];
}

const TagStatsList = ({ tags }: TagStatsListProps) => {
  const setPage = usePageDispatch();
  const handleClick = React.useCallback(() => setPage(0), []);

  if (tags.length === 0) {
    return <p className="text-xs-center">No tags found.</p>;
  }

  return (
    <div className="table-responsive">
      <table className="table tag-stats-table">
        <thead>
          <tr>
            <th scope="col">Rank</th>
            <th scope="col">Tag</th>
            <th scope="col" className="text-xs-right">
              Articles
            </th>
          </tr>
        </thead>
        <tbody>
          {tags.map((tag, index) => {
            const encodedTag = encodeURIComponent(tag.name);
            return (
              <tr key={tag.name}>
                <th scope="row">{index + 1}</th>
                <td>
                  <CustomLink
                    href={`/?tag=${encodedTag}`}
                    as={`/?tag=${encodedTag}`}
                    className="tag-default tag-pill"
                  >
                    <span onClick={handleClick}>{tag.name}</span>
                  </CustomLink>
                </td>
                <td className="text-xs-right">{tag.articleCount}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default TagStatsList;
