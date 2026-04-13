"""Custom exceptions matching Java error response shapes."""


class ValidationError(Exception):
    """Raised for validation errors.

    Mirrors Java's ``InvalidRequestException`` which produces:
      {"errors": {"field": ["message", ...]}}
    at the top level (no extra wrapper).
    """

    def __init__(self, errors: dict[str, list[str]], status_code: int = 422):
        self.errors = errors
        self.status_code = status_code
        super().__init__(str(errors))
