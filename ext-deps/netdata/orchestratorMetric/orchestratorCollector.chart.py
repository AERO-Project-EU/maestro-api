from bases.FrameworkServices.SimpleService import SimpleService
from copy import deepcopy
import json
import errno
import socket

DEFAULT_CONNECT_TIMEOUT = 2.0
DEFAULT_READ_TIMEOUT = 2.0
DEFAULT_WRITE_TIMEOUT = 2.0
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

class Service(SimpleService):
    def __init__(self, configuration=None, name=None):
        self._sock = None
        self._keep_alive = False
        self.host = 'localhost'
        self.port = None
        self.dgram_socket = False
        self.request = ''
        self.__socket_config = None
        self.__empty_request = "".encode()
        SimpleService.__init__(self, configuration=configuration, name=name)
        self.connect_timeout = configuration.get('connect_timeout', DEFAULT_CONNECT_TIMEOUT)
        self.read_timeout = configuration.get('read_timeout', DEFAULT_READ_TIMEOUT)
        self.write_timeout = configuration.get('write_timeout', DEFAULT_WRITE_TIMEOUT)

        self.order = deepcopy(ORDER)
        self.definitions = deepcopy(CHARTS)
        self.database = list()
        self.database.append("example")
        self.data = dict()
        self.previous_data = None

        self.host = self.configuration.get('host', 'localhost')
        self.port = self.configuration.get('port', 38013)


    def _socket_error(self, message=None):    
        if self.__socket_config is not None:
            _, _, _, _, sa = self.__socket_config
            self.error('socket to "{address}" port {port}: {message}'.format(address=sa[0],
                                                                             port=sa[1],
                                                                             message=message))
        else:
            self.error('unknown socket: {0}'.format(message))

    def _connect2socket(self, res=None):
        """
        Connect to a socket, passing the result of getaddrinfo()
        :return: boolean
        """
        if res is None:
            res = self.__socket_config
            if res is None:
                self.error("Cannot create socket to 'None':")
                return False

        af, sock_type, proto, _, sa = res
        try:
            self.debug('Creating socket to "{address}", port {port}'.format(address=sa[0], port=sa[1]))
            self._sock = socket.socket(af, sock_type, proto)
        except socket.error as error:
            self.error('Failed to create socket "{address}", port {port}, error: {error}'.format(address=sa[0],
                                                                                                 port=sa[1],
                                                                                                 error=error))
            self._sock = None
            self.__socket_config = None
            return False

        try:
            self.debug('connecting socket to "{address}", port {port}'.format(address=sa[0], port=sa[1]))
            self._sock.settimeout(self.connect_timeout)
            self.debug('set socket connect timeout to: {0}'.format(self._sock.gettimeout()))
            self._sock.connect(sa)
        except (socket.error, ssl.SSLError) as error:
            self.error('Failed to connect to "{address}", port {port}, error: {error}'.format(address=sa[0],
                                                                                              port=sa[1],
                                                                                              error=error))
            self._disconnect()
            self.__socket_config = None
            return False

        self.debug('connected to "{address}", port {port}'.format(address=sa[0], port=sa[1]))
        self.__socket_config = res
        return True

    def _connect(self):
        """
        Recreate socket and connect to it since sockets cannot be reused after closing
        Available configurations are IPv6, IPv4 or UNIX socket
        :return:
        """
        try:    
            if self.__socket_config is not None:
                self._connect2socket()
            else:
                if self.dgram_socket:
                    sock_type = socket.SOCK_DGRAM
                else:
                    sock_type = socket.SOCK_STREAM
                for res in socket.getaddrinfo(self.host, self.port, socket.AF_UNSPEC, sock_type):
                    if self._connect2socket(res):
                        break

        except Exception as error:
            self.error('unhandled exception during connect : {0}'.format(repr(error)))
            self._sock = None
            self.__socket_config = None

    def _disconnect(self):
        """
        Close socket connection
        :return:
        """
        if self._sock is not None:
            try:
                self.debug('closing socket')
                self._sock.shutdown(2)  # 0 - read, 1 - write, 2 - all
                self._sock.close()
            except Exception as error:
                if not (hasattr(error, 'errno') and error.errno == errno.ENOTCONN):
                    self.error(error)
            self._sock = None

    def _get_raw_data(self):

        if self._sock is None:
            self._connect()
            if self._sock is None:
                return None

        data = ""
        while True:
            self.debug('receiving response')
            try:
                self.debug('set socket read timeout to: {0}'.format(self._sock.gettimeout()))
                self._sock.settimeout(self.read_timeout)
                buf = self._sock.recv(4096)
            except Exception as error:
                self._socket_error('failed to receive response: {0}'.format(error))
                self._disconnect()
                break

            if buf is None or len(buf) == 0:  # handle server disconnect
                if data == "" or data == b"":
                    self.debug("data == null")
                    self._socket_error('unexpectedly disconnected')
                else:
                    self.debug('server closed the connection')
                self._disconnect()
                break

            self.debug('received data')
            data += buf.decode('utf-8', 'ignore')
            if self._check_raw_data(data):
                break

        self.debug('final response: {0}'.format(data))

        if not self._keep_alive:
            self._disconnect()

        return data

    def _check_raw_data(self, data):
        try:
            json_raw = json.loads(raw)
            return True
        except:
            return False

    def _parse_config(self):
        """
        Parse configuration data
        :return: boolean
        """
        try:
            self.unix_socket = str(self.configuration['socket'])
        except (KeyError, TypeError):
            self.debug('No unix socket specified. Trying TCP/IP socket.')
            self.unix_socket = None
            try:
                self.host = str(self.configuration['host'])
            except (KeyError, TypeError):
                self.debug('No host specified. Using: "{0}"'.format(self.host))
            try:
                self.port = int(self.configuration['port'])
            except (KeyError, TypeError):
                self.debug('No port specified. Using: "{0}"'.format(self.port))

        try:
            self.request = str(self.configuration['request'])
        except (KeyError, TypeError):
            self.debug('No request specified. Using: "{0}"'.format(self.request))

        self.request = self.request.encode()

    def check(self):
        return True

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