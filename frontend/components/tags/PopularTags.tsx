import React from "react";

import CustomLink from "../common/CustomLink";
import { TagStats } from "../../lib/types/tagType";

interface PopularTagsProps {
  tags: TagStats[];
}

const PopularTags = ({ tags }: PopularTagsProps) => (
  <div className="table-responsive">
    <table className="table">
      <thead>
        <tr>
          <th scope="col">Rank</th>
          <th scope="col">Tag</th>
          <th scope="col">Articles</th>
        </tr>
      </thead>
      <tbody>
        {tags.map((tag, index) => (
          <tr key={tag.name}>
            <th scope="row">{index + 1}</th>
            <td>
              <CustomLink
                href={`/?tag=${encodeURIComponent(tag.name)}`}
                as={`/?tag=${encodeURIComponent(tag.name)}`}
              >
                {tag.name}
              </CustomLink>
            </td>
            <td>{tag.articleCount}</td>
          </tr>
        ))}
      </tbody>
    </table>
  </div>
);

export default PopularTags;
