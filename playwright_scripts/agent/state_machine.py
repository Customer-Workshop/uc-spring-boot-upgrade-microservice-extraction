"""FSM using transitions library for agent lifecycle management."""

from transitions import Machine, MachineError


class AgentStateMachine:
    """
    States: IDLE -> RECORDING -> PARSING -> READY -> EXECUTING -> REPORTING -> IDLE
    And:    IDLE -> SWAGGER_PARSE -> READY -> EXECUTING -> REPORTING -> IDLE
    Error:  * -> ERROR -> IDLE (via reset)
    """

    STATES = ["IDLE", "RECORDING", "PARSING", "READY", "EXECUTING", "REPORTING", "SWAGGER_PARSE", "ERROR"]

    TRANSITIONS = [
        {"trigger": "start_recording", "source": "IDLE", "dest": "RECORDING"},
        {"trigger": "stop_recording", "source": "RECORDING", "dest": "PARSING"},
        {"trigger": "parsing_done", "source": "PARSING", "dest": "READY"},
        {"trigger": "start_swagger", "source": "IDLE", "dest": "SWAGGER_PARSE"},
        {"trigger": "swagger_done", "source": "SWAGGER_PARSE", "dest": "READY"},
        {"trigger": "start_execution", "source": ["READY", "IDLE"], "dest": "EXECUTING"},
        {"trigger": "execution_done", "source": "EXECUTING", "dest": "REPORTING"},
        {"trigger": "report_done", "source": "REPORTING", "dest": "IDLE"},
        {"trigger": "go_error", "source": "*", "dest": "ERROR"},
        {"trigger": "reset", "source": "*", "dest": "IDLE"},
    ]

    def __init__(self):
        self.machine = Machine(
            model=self,
            states=self.STATES,
            transitions=self.TRANSITIONS,
            initial="IDLE",
            ignore_invalid_triggers=False,
        )
        self._error_reason = ""

    def current_state(self) -> str:
        return self.state

    def safe_fail(self, reason: str = ""):
        """Transition to ERROR state with a reason."""
        self._error_reason = reason
        self.go_error()

    def status_dict(self) -> dict:
        return {
            "state": self.state,
            "name": "PlaywrightAgent",
            "error_reason": self._error_reason if self.state == "ERROR" else "",
        }


# Singleton agent instance
agent = AgentStateMachine()
