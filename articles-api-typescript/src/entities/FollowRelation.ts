import "reflect-metadata";
import { Entity, PrimaryColumn } from "typeorm";

@Entity("follow_relations")
export class FollowRelation {
  @PrimaryColumn("varchar")
  userId: string;

  @PrimaryColumn("varchar")
  targetId: string;

  constructor(userId: string, targetId: string) {
    this.userId = userId ?? "";
    this.targetId = targetId ?? "";
  }
}
