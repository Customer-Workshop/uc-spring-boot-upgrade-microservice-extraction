import "reflect-metadata";
import { Entity, PrimaryColumn, Column } from "typeorm";
import { v4 as uuidv4 } from "uuid";

@Entity("tags")
export class Tag {
  @PrimaryColumn("varchar")
  id: string;

  @Column("varchar")
  name: string;

  constructor(name: string) {
    this.id = uuidv4();
    this.name = name ?? "";
  }
}
