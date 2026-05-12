"""Intent classification via keyword rules (no ML/LLM dependency)."""


INTENT_KEYWORDS = {
    "record": ["record", "recording", "capture", "codegen"],
    "execute": ["run", "execute", "test", "launch"],
    "report": ["report", "results", "history", "logs"],
    "swagger": ["swagger", "openapi", "import", "api", "parse"],
    "locator": ["locator", "selector", "element", "field"],
}


def classify_intent(text: str) -> str:
    """Classify user intent based on keyword matching.

    Returns one of: record, execute, report, swagger, locator, unknown
    """
    text_lower = text.lower().strip()

    scores = {}
    for intent, keywords in INTENT_KEYWORDS.items():
        score = sum(1 for kw in keywords if kw in text_lower)
        if score > 0:
            scores[intent] = score

    if not scores:
        return "unknown"

    return max(scores, key=scores.get)


def extract_parameters(text: str, intent: str) -> dict:
    """Extract basic parameters from text based on intent."""
    params = {}

    if intent == "execute":
        import re
        tc_match = re.search(r"tc[_-]?(\d+)", text, re.IGNORECASE)
        if tc_match:
            params["tc_id"] = int(tc_match.group(1))
        if "headless" in text.lower():
            params["headless"] = True
        if "headed" in text.lower():
            params["headless"] = False

    elif intent == "swagger":
        import re
        url_match = re.search(r"https?://[^\s]+", text)
        if url_match:
            params["url"] = url_match.group(0)

    return params
