import json
import urllib.request

data = {
    'login': 't2',
    'password': 't2'
}

params = json.dumps(data).encode('utf8')
req = urllib.request.Request('http://localhost:8080/account/create', data=params,
                                     headers={'content-type': 'application/json'})
response = urllib.request.urlopen(req)

