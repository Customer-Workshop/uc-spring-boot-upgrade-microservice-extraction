import "reflect-metadata";
import { Entity, PrimaryColumn, Column } from "typeorm";
import { v4 as uuidv4 } from "uuid";

@Entity("users")
export class User {
  @PrimaryColumn("varchar")
  id: string;

  @Column("varchar")
  email: string;

  @Column("varchar")
  username: string;

  @Column("varchar")
  password: string;

  @Column("varchar", { nullable: true })
  bio: string;

  @Column("varchar", { nullable: true })
  image: string;

  constructor(
    email: string,
    username: string,
    password: string,
    bio?: string,
    image?: string
  ) {
    this.id = uuidv4();
    this.email = email ?? "";
    this.username = username ?? "";
    this.password = password ?? "";
    this.bio = bio ?? "";
    this.image = image ?? "";
  }
}
