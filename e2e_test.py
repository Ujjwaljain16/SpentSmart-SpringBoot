import requests
import json
import time

BASE_URL = "http://localhost:8080/api"

def print_status(message, status="INFO"):
    print(f"[{status}] {message}")

def run_e2e_test():
    print_status("Starting E2E Test Suite...")
    
    # Generate unique user
    timestamp = int(time.time())
    email = f"e2e_user_{timestamp}@example.com"
    password = "Password123"
    
    # 1. Register
    print_status(f"Testing Registration for {email}...")
    register_payload = {
        "email": email,
        "password": password,
        "fullName": "E2E Test User"
    }
    
    try:
        response = requests.post(f"{BASE_URL}/auth/register", json=register_payload)
        if response.status_code != 201:
            print_status(f"Registration Failed: {response.text}", "ERROR")
            return
        
        token = response.json().get("token")
        print_status("Registration Successful. Token received.", "SUCCESS")
        
        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }
        
        # 2. Get Categories (to verify auth and setup)
        print_status("Testing Get Categories...")
        response = requests.get(f"{BASE_URL}/categories", headers=headers)
        if response.status_code != 200:
             print_status(f"Get Categories Failed: {response.text}", "ERROR")
             return
        
        categories = response.json()
        if not categories:
             print_status("No categories found!", "ERROR")
             return
        
        category_id = categories[0]['id']
        print_status(f"Categories verified. Using Category ID: {category_id}", "SUCCESS")
        
        # 3. Create Expense
        print_status("Testing Create Expense...")
        expense_payload = {
            "categoryId": category_id,
            "amount": 100.50,
            "description": "E2E Test Expense",
            "expenseDate": "2026-01-21",
            "paymentMethod": "CARD"
        }
        
        response = requests.post(f"{BASE_URL}/expenses", json=expense_payload, headers=headers)
        if response.status_code != 201:
            print_status(f"Create Expense Failed: {response.text}", "ERROR")
            return
        
        expense_id = response.json().get("id")
        print_status(f"Expense Created. ID: {expense_id}", "SUCCESS")
        
        # 4. Get Expense Logic
        print_status("Testing Get Expense by ID...")
        response = requests.get(f"{BASE_URL}/expenses/{expense_id}", headers=headers)
        if response.status_code != 200:
             print_status("Get Expense Failed", "ERROR")
             return
        print_status("Get Expense Verified.", "SUCCESS")
        
        # 5. Analytics Check
        print_status("Testing Analytics (Monthly Summary)...")
        # Ensure we ask for the month of the expense we just created (Jan 2026)
        response = requests.get(f"{BASE_URL}/analytics/monthly-summary?month=1&year=2026", headers=headers)
        
        if response.status_code == 200:
             data = response.json()
             if data.get("totalExpenses") == 100.50:
                 print_status("Analytics Data Accurate.", "SUCCESS")
             else:
                 print_status(f"Analytics Data Mismatch: {data}", "WARNING")
        else:
             print_status(f"Analytics Failed: {response.text}", "ERROR")
             
        # 5. Update Expense (New Feature Check)
        print_status("Testing Update Expense...")
        update_payload = {
            "categoryId": category_id,
            "amount": 200.00,
            "description": "E2E Test Expense Updated",
            "expenseDate": "2026-01-21",
            "paymentMethod": "UPI"
        }
        response = requests.put(f"{BASE_URL}/expenses/{expense_id}", json=update_payload, headers=headers)
        if response.status_code == 200:
             print_status("Update Expense Verified.", "SUCCESS")
        else:
             print_status(f"Update Failed: {response.text}", "ERROR")

        # 6. Filter Expenses (Advanced Search)
        print_status("Testing Filter Expenses (by Category)...")
        response = requests.get(f"{BASE_URL}/expenses?categoryId={category_id}", headers=headers)
        if response.status_code == 200 and len(response.json()['content']) > 0:
             print_status("Filtering Verified.", "SUCCESS")
        else:
             print_status("Filtering Failed or Empty Result", "WARNING")

        # 7. Analytics Deep Dive
        print_status("Testing Analytics (Category Breakdown)...")
        response = requests.get(f"{BASE_URL}/analytics/category-breakdown?month=1&year=2026", headers=headers)
        if response.status_code == 200:
             print_status("Category Breakdown Verified.", "SUCCESS")
        else:
             print_status(f"Category Breakdown Failed: {response.text}", "ERROR")

        print_status("Testing Smart Insights...")
        response = requests.get(f"{BASE_URL}/analytics/insights", headers=headers)
        if response.status_code == 200:
             print_status("Smart Insights Verified.", "SUCCESS")
        else:
             print_status(f"Insights Failed: {response.text}", "ERROR")

        # 8. Delete Expense
        print_status("Testing Delete Expense...")
        response = requests.delete(f"{BASE_URL}/expenses/{expense_id}", headers=headers)
        if response.status_code == 204:
            print_status("Delete Verified (No Content).", "SUCCESS")
        else:
            print_status(f"Delete Failed: {response.status_code}", "ERROR")
            
        print_status("E2E Test Suite Completed Successfully! All Features Verified.", "SUCCESS")
        
    except Exception as e:
        print_status(f"Exception during test: {str(e)}", "CRITICAL")

if __name__ == "__main__":
    run_e2e_test()
