from sqlalchemy import Column, Integer, String

from app.database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String(20), unique=True, nullable=False, index=True)
    name = Column(String(200), nullable=False)
    email = Column(String(200), nullable=True)
    hashed_password = Column(String(200), nullable=False)
