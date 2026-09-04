from bases.FrameworkServices.SocketService import SocketService
from copy import deepcopy
import json

priority = 60000
retries = 60

ORDER = ["example"]

CHARTS = {
    "example": {
        "options": ["example", "example", "example", "example", "example", "stacked"],
        "lines": [
             ["example", "example", "absolute", 1, 1]
        ]
    }
}


class Service(SocketService):
    def __init__(self, configuration=None, name=None):
        SocketService.__init__(self, configuration=configuration, name=name)
        # self._keep_alive = True

        self.order = deepcopy(ORDER)
        self.definitions = deepcopy(CHARTS)
        self.database = list()
        self.database.append("example")
        self.data = dict()
        self.previous_data = None

        self.host = self.configuration.get('host', 'localhost')
        self.port = self.configuration.get('port', 38013)

    def get_data(self):
        """
        Get data from socket
        :return: dict
        """

        try:
            raw = self._get_raw_data()
        except (ValueError, AttributeError):
            self.error('Collector returned ValueError or AttributeError')
            return self.previous_data

        if raw is None:
            self.error('Collector returned no data')
            return self.previous_data

        try:
            json_raw = json.loads(raw)
        except:
            self.error("Couldn't load the raw data to json")
            return self.previous_data

        if len(json_raw) == 0:
            self.error('Collector returned empty list')
            return self.previous_data

        if "example" in self.database:
            self.database.remove("example")
            ORDER.remove("example")
            CHARTS.pop("example")

        # Iterate through all the metric
        for metricID in json_raw.keys():
            # check if we have this metric ID in our database
            if metricID not in self.database:
                # if not added to the order and to the definitions too
                ORDER.append(metricID)
                self.database.append(metricID)

                chart = dict()
                chart["options"] = json_raw[metricID]["options"]

                lines = list()
                for line in json_raw[metricID]["lines"]:
                    self.data[line[0]] = line.pop(5)
                    lines.append(line)

                chart["lines"] = lines
                CHARTS[metricID] = chart
            else:
                for line in json_raw[metricID]["lines"]:
                    if not self.data.has_key(line[0]):
                        self.data[line[0]] = line.pop(5)
                        CHARTS[metricID]["lines"].append(line)
                    else:
                        self.data[line[0]] = line.pop(5)

        self.order = ORDER
        self.definitions = CHARTS
        self.create()

        self.previous_data = deepcopy(self.data)
        return self.data

    def check(self):
        return True