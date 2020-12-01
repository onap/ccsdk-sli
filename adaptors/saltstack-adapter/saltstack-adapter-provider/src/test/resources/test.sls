{
  "equipment-data": [
    {
      "server-count": "4",
      "max-server-speed": "1600000",
      "number-primary-servers": "2",
      "equipment-id": "Server1",
      "server-model": "Unknown",
      "server-id": "Server1",
      "test-node" : {
        "test-inner-node" : "Test-Value"
      }
    }
  ],
  "resource-state": {
    "threshold-value": "1600000",
    "last-added": "1605000",
    "used": "1605000",
    "limit-value": "1920000"
  },
  "resource-rule": {
    "endpoint-position": "VCE-Cust",
    "soft-limit-expression": "0.6 * max-server-speed * number-primary-servers",
    "resource-name": "Bandwidth",
    "service-model": "DUMMY",
    "hard-limit-expression": "max-server-speed * number-primary-servers",
    "equipment-level": "Server"
  },
  "message": "The provisioned access bandwidth is at or exceeds 50% of the total server capacity."
}
