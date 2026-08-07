export interface TagList {
  tags: string[];
}

export interface TagStat {
  name: string;
  articleCount: number;
}

export interface TagStatsList {
  tags: TagStat[];
}
