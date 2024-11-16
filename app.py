import bluetooth

server_sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
server_sock.bind(("", bluetooth.PORT_ANY))
server_sock.listen(1)

print("Waiting for connection...")
client_sock, address = server_sock.accept()
print(f"Connected to {address}")

while True:
    data = client_sock.recv(1024).decode('utf-8')
    print(f"Received: {data}")

    if data == "TURN_ON":
        # Add logic to enable mobile data
        print("Turning ON data...")
    elif data == "TURN_OFF":
        # Add logic to disable mobile data
        print("Turning OFF data...")

client_sock.close()
server_sock.close()
