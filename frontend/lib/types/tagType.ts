export interface TagList {
  tags: string[];
}

export interface TagStats {
  name: string;
  articleCount: number;
}

export interface TagStatsResponse {
  tags: TagStats[];
}
