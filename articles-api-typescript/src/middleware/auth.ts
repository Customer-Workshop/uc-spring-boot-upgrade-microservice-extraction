import { Request, Response, NextFunction } from "express";
import { DataSource } from "typeorm";
import { User } from "../entities/User";

/**
 * Auth stub: attaches the first user from the DB to req.user.
 *
 * This is a simplified auth mechanism for development/testing purposes.
 * In production, this would be replaced with JWT-based authentication
 * matching the Java version's JwtTokenFilter.
 */
export function authMiddleware(dataSource: DataSource) {
  return async (req: Request, _res: Response, next: NextFunction) => {
    try {
      const userRepo = dataSource.getRepository(User);
      const user = await userRepo.findOne({ where: {}, order: { username: "ASC" } });
      if (user) {
        (req as AuthenticatedRequest).user = user;
      }
    } catch {
      // silently continue — user will be undefined
    }
    next();
  };
}

/**
 * Middleware that requires authentication. Returns 401 if no user is found.
 */
export function requireAuth(req: Request, res: Response, next: NextFunction) {
  const user = (req as AuthenticatedRequest).user;
  if (!user) {
    res.status(401).json({ errors: { body: ["authorization required"] } });
    return;
  }
  next();
}

export interface AuthenticatedRequest extends Request {
  user?: User;
}
