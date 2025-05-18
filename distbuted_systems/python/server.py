import asyncio
import json
import logging
import uuid

import websockets

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)

CLIENTS = {}


async def broadcast(message, exclude_ws=None):
    for ws, client_id in list(CLIENTS.items()):
        if ws == exclude_ws:
            continue
        try:
            await ws.send(message)
            logger.debug(f"Sent to {client_id}")
        except websockets.ConnectionClosed:
            await disconnect_client(ws, client_id)


async def disconnect_client(ws, client_id):
    if ws in CLIENTS:
        del CLIENTS[ws]
        logger.info(f"{client_id} disconnected. {len(CLIENTS)} clients")
        await broadcast(json.dumps({
            "type": "info",
            "message": f"{client_id} disconnected"
        }))


async def handle_client(ws):
    client_id = str(uuid.uuid4())
    CLIENTS[ws] = client_id
    logger.info(f"{client_id} connected from {ws.remote_address[0]}")

    try:
        # Send welcome message
        await ws.send(json.dumps({
            "type": "info",
            "message": "Connected",
            "client_id": client_id
        }))

        # Handle incoming messages
        async for msg in ws:
            logger.info(f"Received from {client_id}: {msg}")
            try:
                data = json.loads(msg)
                # Validate required fields
                if not all(key in data for key in ["id", "content"]):
                    await ws.send(json.dumps({
                        "type": "error",
                        "message": "Missing id or content"
                    }))
                    continue
                # Broadcast message
                await broadcast(json.dumps({
                    "type": "message",
                    "sender_id": client_id,
                    "data": data
                }), exclude_ws=ws)
                # Send acknowledgment
                await ws.send(json.dumps({
                    "type": "ack",
                    "message_id": data["id"]
                }))
            except json.JSONDecodeError:
                logger.warning(f"Invalid JSON from {client_id}")
                await ws.send(json.dumps({
                    "type": "error",
                    "message": "Invalid JSON"
                }))
    except websockets.ConnectionClosed:
        logger.info(f"{client_id} connection closed")
    finally:
        await disconnect_client(ws, client_id)


async def main():
    host, port = "0.0.0.0", 8765
    logger.info(f"Starting server on ws://{host}:{port}")
    async with websockets.serve(handle_client, host, port):
        await asyncio.Future()


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Server shutting down")
