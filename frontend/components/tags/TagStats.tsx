import React from "react";

import CustomLink from "../common/CustomLink";
import { usePageDispatch } from "../../lib/context/PageContext";
import { TagStat } from "../../lib/types/tagType";

interface TagStatsProps {
  tags: TagStat[];
}

const TagStats = ({ tags }: TagStatsProps) => {
  const setPage = usePageDispatch();
  const handleClick = React.useCallback(() => setPage(0), []);

  return (
    <div className="table-responsive">
      <table className="table table-striped">
        <thead>
          <tr>
            <th scope="col">Rank</th>
            <th scope="col">Tag</th>
            <th scope="col">Articles</th>
          </tr>
        </thead>
        <tbody>
          {tags.map((tag, index) => {
            const encodedName = encodeURIComponent(tag.name);
            return (
              <tr key={tag.name}>
                <th scope="row">{index + 1}</th>
                <td>
                  <CustomLink
                    href={`/?tag=${encodedName}`}
                    as={`/?tag=${encodedName}`}
                  >
                    <span onClick={handleClick}>{tag.name}</span>
                  </CustomLink>
                </td>
                <td>{tag.articleCount}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default TagStats;
