# Linked List -- NeetCode 150

```java
// Common node definition used throughout this file
class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int x) { val = x; }
    ListNode(int x, ListNode n) { val = x; next = n; }
}
```

---

## Problem 35: Reverse Linked List
**LeetCode #206** | Reverse a singly linked list.

### Brute Force -- O(N) time, O(N) space (store in array, rebuild)
```java
ListNode reverseList(ListNode head) {
    List<Integer> vals = new ArrayList<>();
    for (ListNode p = head; p != null; p = p.next) vals.add(p.val);
    ListNode cur = head;
    for (int i = vals.size() - 1; i >= 0; i--) { cur.val = vals.get(i); cur = cur.next; }
    return head;
}
```

### Optimal -- O(N) time, O(1) space (iterative)
```java
ListNode reverseList(ListNode head) {
    ListNode prev = null, cur = head;
    while (cur != null) { ListNode nxt = cur.next; cur.next = prev; prev = cur; cur = nxt; }
    return prev;
}
```

---

## Problem 36: Merge Two Sorted Lists
**LeetCode #21** | Merge two sorted linked lists.

### Brute Force -- O(N+M) time, O(N+M) space (collect all, sort, rebuild)
```java
ListNode mergeTwoLists(ListNode l1, ListNode l2) {
    List<Integer> vals = new ArrayList<>();
    for (ListNode p = l1; p != null; p = p.next) vals.add(p.val);
    for (ListNode p = l2; p != null; p = p.next) vals.add(p.val);
    Collections.sort(vals);
    ListNode dummy = new ListNode(), cur = dummy;
    for (int v : vals) { cur.next = new ListNode(v); cur = cur.next; }
    return dummy.next;
}
```

### Optimal -- O(N+M) time, O(1) space (in-place merge)
```java
ListNode mergeTwoLists(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(), cur = dummy;
    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) { cur.next = l1; l1 = l1.next; }
        else                  { cur.next = l2; l2 = l2.next; }
        cur = cur.next;
    }
    cur.next = (l1 != null) ? l1 : l2;
    return dummy.next;
}
```

---

## Problem 37: Reorder List
**LeetCode #143** | Reorder list: L0->Ln->L1->Ln-1->L2->Ln-2...

### Brute Force -- O(N) time, O(N) space (store in array)
```java
void reorderList(ListNode head) {
    List<ListNode> nodes = new ArrayList<>();
    for (ListNode p = head; p != null; p = p.next) nodes.add(p);
    int l = 0, r = nodes.size() - 1;
    while (l < r) {
        nodes.get(l).next = nodes.get(r); l++;
        if (l == r) break;
        nodes.get(r).next = nodes.get(l); r--;
    }
    nodes.get(l).next = null;
}
```

### Optimal -- O(N) time, O(1) space (find mid, reverse second half, merge)
```java
void reorderList(ListNode head) {
    if (head == null || head.next == null) return;
    // 1. Find mid
    ListNode slow = head, fast = head;
    while (fast.next != null && fast.next.next != null) { slow = slow.next; fast = fast.next.next; }
    // 2. Reverse second half
    ListNode prev = null, cur = slow.next;
    slow.next = null;
    while (cur != null) { ListNode nxt = cur.next; cur.next = prev; prev = cur; cur = nxt; }
    // 3. Merge two halves
    ListNode first = head, second = prev;
    while (second != null) {
        ListNode fn = first.next, sn = second.next;
        first.next = second; second.next = fn;
        first = fn; second = sn;
    }
}
```

---

## Problem 38: Remove Nth Node From End of List
**LeetCode #19** | Remove the nth node from the end.

### Brute Force -- O(N) time, two passes
```java
ListNode removeNthFromEnd(ListNode head, int n) {
    int len = 0;
    for (ListNode p = head; p != null; p = p.next) len++;
    ListNode dummy = new ListNode(0, head), cur = dummy;
    for (int i = 0; i < len - n; i++) cur = cur.next;
    cur.next = cur.next.next;
    return dummy.next;
}
```

### Optimal -- O(N) time, O(1) space (one pass, two pointers)
```java
ListNode removeNthFromEnd(ListNode head, int n) {
    ListNode dummy = new ListNode(0, head);
    ListNode fast = dummy, slow = dummy;
    for (int i = 0; i <= n; i++) fast = fast.next;
    while (fast != null) { fast = fast.next; slow = slow.next; }
    slow.next = slow.next.next;
    return dummy.next;
}
```

---

## Problem 39: Copy List with Random Pointer
**LeetCode #138** | Deep copy a linked list where each node has next and random pointer.

```java
// Node definition for this problem
class Node {
    int val;
    Node next, random;
    Node(int v) { val = v; }
}
```

### Brute Force -- O(N) time, O(N) space (two passes with map)
```java
Node copyRandomList(Node head) {
    Map<Node, Node> mp = new HashMap<>();
    for (Node p = head; p != null; p = p.next) mp.put(p, new Node(p.val));
    for (Node p = head; p != null; p = p.next) {
        mp.get(p).next   = mp.get(p.next);
        mp.get(p).random = mp.get(p.random);
    }
    return mp.get(head);
}
```

### Optimal -- O(N) time, O(1) space (interleave old and new nodes)
```java
Node copyRandomList(Node head) {
    if (head == null) return null;
    // Step 1: interleave: A->A'->B->B'->...
    for (Node p = head; p != null; p = p.next.next) {
        Node copy = new Node(p.val);
        copy.next = p.next; p.next = copy;
    }
    // Step 2: set random pointers
    for (Node p = head; p != null; p = p.next.next)
        if (p.random != null) p.next.random = p.random.next;
    // Step 3: separate lists
    Node dummy = new Node(0), cur = dummy;
    for (Node p = head; p != null; p = p.next) {
        cur.next = p.next; cur = cur.next; p.next = cur.next;
    }
    return dummy.next;
}
```

---

## Problem 40: Add Two Numbers
**LeetCode #2** | Add two numbers represented as reversed linked lists.

### Brute Force -- O(N) time (same as optimal, no simpler approach)
```java
// Extract numbers as strings, add, convert back (overflow prone for large nums)
```

### Optimal -- O(max(M,N)) time, O(max(M,N)) space (digit-by-digit with carry)
```java
ListNode addTwoNumbers(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(), cur = dummy;
    int carry = 0;
    while (l1 != null || l2 != null || carry != 0) {
        int sum = carry;
        if (l1 != null) { sum += l1.val; l1 = l1.next; }
        if (l2 != null) { sum += l2.val; l2 = l2.next; }
        carry = sum / 10;
        cur.next = new ListNode(sum % 10);
        cur = cur.next;
    }
    return dummy.next;
}
```

---

## Problem 41: Linked List Cycle
**LeetCode #141** | Detect if linked list has a cycle.

### Brute Force -- O(N) time, O(N) space (store visited nodes)
```java
boolean hasCycle(ListNode head) {
    Set<ListNode> seen = new HashSet<>();
    while (head != null) { if (!seen.add(head)) return true; head = head.next; }
    return false;
}
```

### Optimal -- O(N) time, O(1) space (Floyd's cycle detection)
```java
boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next; fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}
```

---

## Problem 42: Find The Duplicate Number
**LeetCode #287** | Array of N+1 integers where each is in [1,N]. Find the duplicate without modifying array.

### Brute Force -- O(N^2) time, O(1) extra space
```java
int findDuplicate(int[] nums) {
    int n = nums.length;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            if (nums[i] == nums[j]) return nums[i];
    return -1;
}
```

### Optimal -- O(N) time, O(1) space (Floyd's cycle detection on implicit linked list)
```java
int findDuplicate(int[] nums) {
    int slow = nums[0], fast = nums[0];
    do { slow = nums[slow]; fast = nums[nums[fast]]; } while (slow != fast);
    slow = nums[0];
    while (slow != fast) { slow = nums[slow]; fast = nums[fast]; }
    return slow;
}
```

---

## Problem 43: LRU Cache
**LeetCode #146** | Implement LRU Cache with get and put in O(1).

### Brute Force -- O(N) put/get (scan list for LRU)
```java
// Uses ordered-map approach: O(N) for eviction
```

### Optimal -- O(1) time (doubly linked list + hash map)
```java
class LRUCache {
    // LinkedHashMap in access-order mode gives O(1) get/put with automatic LRU eviction.
    private final LinkedHashMap<Integer, Integer> map;
    public LRUCache(int capacity) {
        map = new LinkedHashMap<>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };
    }
    public int get(int key) {
        return map.getOrDefault(key, -1);
    }
    public void put(int key, int value) {
        map.put(key, value);
    }
}
```

---

## Problem 44: Merge K Sorted Lists
**LeetCode #23** | Merge K sorted linked lists into one.

### Brute Force -- O(N*K) time (repeatedly merge pairs one by one)
```java
ListNode mergeTwo(ListNode a, ListNode b) {
    ListNode d = new ListNode(), c = d;
    while (a != null && b != null) {
        if (a.val <= b.val) { c.next = a; a = a.next; } else { c.next = b; b = b.next; }
        c = c.next;
    }
    c.next = (a != null) ? a : b;
    return d.next;
}
ListNode mergeKLists(ListNode[] lists) {
    ListNode res = null;
    for (ListNode l : lists) res = mergeTwo(res, l);
    return res;
}
```

### Optimal -- O(N log K) time (min-heap)
```java
ListNode mergeKLists(ListNode[] lists) {
    PriorityQueue<ListNode> pq = new PriorityQueue<>((a, b) -> a.val - b.val);
    for (ListNode l : lists) if (l != null) pq.offer(l);
    ListNode dummy = new ListNode(), cur = dummy;
    while (!pq.isEmpty()) {
        cur.next = pq.poll();
        cur = cur.next;
        if (cur.next != null) pq.offer(cur.next);
    }
    return dummy.next;
}
```

---

## Problem 45: Reverse Nodes in K-Group
**LeetCode #25** | Reverse the list in groups of k nodes.

### Brute Force -- O(N) time, O(N) space (collect vals, reverse in blocks)
```java
ListNode reverseKGroup(ListNode head, int k) {
    List<Integer> vals = new ArrayList<>();
    for (ListNode p = head; p != null; p = p.next) vals.add(p.val);
    for (int i = 0; i + k <= vals.size(); i += k)
        Collections.reverse(vals.subList(i, i + k));
    ListNode p = head;
    for (int v : vals) { p.val = v; p = p.next; }
    return head;
}
```

### Optimal -- O(N) time, O(1) space (in-place group reversal)
```java
ListNode reverseKGroup(ListNode head, int k) {
    // Check if k nodes remain
    ListNode check = head;
    for (int i = 0; i < k; i++) { if (check == null) return head; check = check.next; }
    // Reverse k nodes
    ListNode prev = null, cur = head;
    for (int i = 0; i < k; i++) { ListNode nxt = cur.next; cur.next = prev; prev = cur; cur = nxt; }
    head.next = reverseKGroup(cur, k); // head is now tail of reversed group
    return prev;
}
```
