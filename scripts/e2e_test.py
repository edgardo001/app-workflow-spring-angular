import os
import re
import time
import requests
import jwt

# Load .env
env = {}
env_path = r"C:\Users\datasoft\Desktop\edgardo001.github.com\app-workflow-spring-angular\.env"
if os.path.exists(env_path):
    with open(env_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith("#"):
                parts = line.split("=", 1)
                if len(parts) == 2:
                    key = parts[0].strip()
                    val = parts[1].strip()
                    if val.startswith('"') and val.endswith('"'):
                        val = val[1:-1]
                    env[key] = val

secret_key = env.get("JWT_SECRET", "change-this-to-a-long-random-base64-secret-key")
print(f"Loaded JWT secret from .env: {secret_key[:10]}...")

base_url = "http://localhost:8080"

# 1. Generate Auth Token for owner@example.com
auth_payload = {
    "sub": "owner@example.com",
    "name": "Owner User",
    "avatarUrl": "https://avatar.example.com/owner",
    "role": "USER",
    "iat": int(time.time()),
    "exp": int(time.time()) + 86400
}
auth_token = jwt.encode(auth_payload, secret_key, algorithm="HS256")
headers = {"Authorization": f"Bearer {auth_token}"}
cookies = {"auth_token": auth_token}

print("\n--- Phase 1: Uploading document ---")
file_content = b"This is a dummy PDF file content for approval flow testing."
files = {"file": ("dummy_doc.pdf", file_content, "application/pdf")}
upload_res = requests.post(f"{base_url}/api/documents/upload", files=files, headers=headers, cookies=cookies)
if upload_res.status_code != 200:
    print(f"Failed to upload document. Status: {upload_res.status_code}, Body: {upload_res.text}")
    exit(1)

doc_meta = upload_res.json()
doc_id = doc_meta["id"]
print(f"Document uploaded successfully! ID: {doc_id}, Name: {doc_meta['fileName']}")

print("\n--- Phase 2: Creating Flow ---")
flow_payload = {
    "title": "E2E Integration Test Flow",
    "description": "Validating sequential approval flow and pending list endpoints",
    "deadline": "2026-06-30T12:00:00Z",
    "participantEmails": ["approver1@example.com", "approver2@example.com"],
    "documentIds": [doc_id]
}
create_res = requests.post(f"{base_url}/api/flows", json=flow_payload, headers=headers, cookies=cookies)
if create_res.status_code != 200:
    print(f"Failed to create flow. Status: {create_res.status_code}, Body: {create_res.text}")
    exit(1)

flow = create_res.json()
flow_id = flow["id"]
print(f"Flow created successfully! ID: {flow_id}, Title: {flow['title']}, Status: {flow['status']}")

print(f"Current step order: {flow.get('step')}, Total steps: {flow.get('totalSteps')}")
print("Participants:")
for p in flow.get("participants", []):
    print(f" - {p['name']} ({p['email']}): Status={p['status']}, Order={p['stepOrder']}")

print("\n--- Phase 3: Verify JWS Verification for Step 1 ---")
token1 = jwt.encode({
    "sub": flow_id,
    "email": "approver1@example.com",
    "iat": int(time.time()),
    "exp": int(time.time()) + 86400
}, secret_key, algorithm="HS256")

verify_res = requests.get(f"{base_url}/api/flows/verify", params={"token": token1})
if verify_res.status_code != 200:
    print(f"Token verification failed! Status: {verify_res.status_code}, Body: {verify_res.text}")
    exit(1)
print(f"Token verified successfully! Flow Title returned: {verify_res.json()['title']}")

print("\n--- Phase 4: Approve Step 1 using JWS token and Authentication ---")
# 1. Generate Auth Token for approver1@example.com
auth_payload1 = {
    "sub": "approver1@example.com",
    "name": "First Approver",
    "avatarUrl": "https://avatar.example.com/approver1",
    "role": "USER",
    "iat": int(time.time()),
    "exp": int(time.time()) + 86400
}
auth_token1 = jwt.encode(auth_payload1, secret_key, algorithm="HS256")
headers1 = {"Authorization": f"Bearer {auth_token1}"}
cookies1 = {"auth_token": auth_token1}

# Test Security Constraint: Try approving with owner@example.com (mismatched user)
print("Testing security constraint: approving with mismatched user (owner@example.com)...")
action_payload = {"token": token1}
mismatched_res = requests.post(f"{base_url}/api/flows/{flow_id}/approve", json=action_payload, headers=headers, cookies=cookies)
if mismatched_res.status_code in (403, 500):
    print(f"Success: Mismatched approval blocked! Status: {mismatched_res.status_code}")
else:
    print(f"Error: Mismatched approval was allowed! Status: {mismatched_res.status_code}, Body: {mismatched_res.text}")
    exit(1)

# Approve with correct user (approver1@example.com)
print("Approving with matching user (approver1@example.com)...")
approve1_res = requests.post(f"{base_url}/api/flows/{flow_id}/approve", json=action_payload, headers=headers1, cookies=cookies1)
if approve1_res.status_code != 200:
    print(f"Failed to approve step 1. Status: {approve1_res.status_code}, Body: {approve1_res.text}")
    exit(1)

flow = approve1_res.json()
print(f"Step 1 approved successfully! Status: {flow['status']}, Current step order: {flow.get('step')}")
for p in flow.get("participants", []):
    print(f" - {p['name']} ({p['email']}): Status={p['status']}, Order={p['stepOrder']}")

print("\n--- Phase 5: Approve Step 2 using token-less authenticated request ---")
auth_payload2 = {
    "sub": "approver2@example.com",
    "name": "Second Approver",
    "avatarUrl": "https://avatar.example.com/approver2",
    "role": "USER",
    "iat": int(time.time()),
    "exp": int(time.time()) + 86400
}
auth_token2 = jwt.encode(auth_payload2, secret_key, algorithm="HS256")
headers2 = {"Authorization": f"Bearer {auth_token2}"}
cookies2 = {"auth_token": auth_token2}

pending_res = requests.get(f"{base_url}/api/flows/pending", headers=headers2, cookies=cookies2)
if pending_res.status_code != 200:
    print(f"Failed to get pending flows. Status: {pending_res.status_code}, Body: {pending_res.text}")
    exit(1)

pending_flows = pending_res.json()
found = False
for f in pending_flows:
    if f["id"] == flow_id:
        found = True
        print(f"Success: Flow found in pending flows for approver2! isMyTurn={f.get('isMyTurn')}")

if not found:
    print("Error: Flow not found in pending list for approver2!")
    exit(1)

approve2_res = requests.post(f"{base_url}/api/flows/{flow_id}/approve", json={"token": None}, headers=headers2, cookies=cookies2)
if approve2_res.status_code != 200:
    print(f"Failed to approve step 2. Status: {approve2_res.status_code}, Body: {approve2_res.text}")
    exit(1)

flow = approve2_res.json()
print(f"Step 2 approved successfully! Status: {flow['status']}, Current step order: {flow.get('step')}")
for p in flow.get("participants", []):
    print(f" - {p['name']} ({p['email']}): Status={p['status']}, Order={p['stepOrder']}")

if flow['status'] == "COMPLETED":
    print("\nE2E INTEGRATION TEST PASSED! The sequential approval flow works flawlessly!")
else:
    print(f"\nE2E INTEGRATION TEST FAILED: Flow status is {flow['status']}, expected COMPLETED")
    exit(1)
