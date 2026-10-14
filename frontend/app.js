// CineThread Concurrency Simulator
const seatsData = [];

// 8 VIP seats
for (let i = 1; i <= 8; i++) {
  seatsData.push({ id: `VIP-${i}`, tier: 'VIP', price: 75, booked: false, bookedBy: null, locking: false });
}
// 16 Regular seats
for (let i = 1; i <= 16; i++) {
  seatsData.push({ id: `REG-${i}`, tier: 'REGULAR', price: 35, booked: false, bookedBy: null, locking: false });
}

let conflictsCount = 0;
let isSimulating = false;

// DOM
const vipRow = document.getElementById('vipRow');
const regularRow1 = document.getElementById('regularRow1');
const regularRow2 = document.getElementById('regularRow2');
const terminalBox = document.getElementById('terminalBox');
const bookedSeatsEl = document.getElementById('bookedSeats');
const availableSeatsEl = document.getElementById('availableSeats');
const conflictsPreventedEl = document.getElementById('conflictsPrevented');
const threadEngineStatus = document.getElementById('threadEngineStatus');

function logTerminal(text, type = '') {
  const line = document.createElement('div');
  line.className = `terminal-line ${type}`;
  line.textContent = text;
  terminalBox.prepend(line);
}

function updateTelemetry() {
  const booked = seatsData.filter(s => s.booked).length;
  bookedSeatsEl.textContent = booked;
  availableSeatsEl.textContent = seatsData.length - booked;
  conflictsPreventedEl.textContent = conflictsCount;
}

function renderSeats() {
  vipRow.innerHTML = '';
  regularRow1.innerHTML = '';
  regularRow2.innerHTML = '';

  seatsData.forEach((seat, idx) => {
    const el = document.createElement('div');
    el.className = `seat ${seat.tier === 'VIP' ? 'vip' : ''} ${seat.booked ? 'booked' : ''} ${seat.locking ? 'locking' : ''}`;
    el.dataset.id = seat.id;
    el.innerHTML = `
      <span>${seat.id}</span>
      ${seat.booked ? `<span class="seat-user">${seat.bookedBy}</span>` : ''}
    `;

    el.addEventListener('click', () => {
      if (!seat.booked && !seat.locking && !isSimulating) {
        manualBook(seat.id, 'User-Direct');
      }
    });

    if (seat.tier === 'VIP') {
      vipRow.appendChild(el);
    } else if (idx < 16) {
      regularRow1.appendChild(el);
    } else {
      regularRow2.appendChild(el);
    }
  });

  updateTelemetry();
}

function manualBook(seatId, customerName) {
  const seat = seatsData.find(s => s.id === seatId);
  if (!seat || seat.booked) return;

  seat.locking = true;
  renderSeats();
  logTerminal(`[Thread-Main] Acquiring lock for seat ${seatId}...`, 'info');

  setTimeout(() => {
    seat.locking = false;
    seat.booked = true;
    seat.bookedBy = customerName;
    renderSeats();
    logTerminal(`[SUCCESS] Seat ${seatId} booked by ${customerName} ($${seat.price})`, 'success');
  }, 250);
}

// Concurrent Simulation with simulated race conditions
document.getElementById('simulateThreadsBtn').addEventListener('click', () => {
  if (isSimulating) return;
  isSimulating = true;
  threadEngineStatus.textContent = 'Simulating 16 Threads...';
  threadEngineStatus.style.background = 'rgba(244, 63, 94, 0.2)';
  threadEngineStatus.style.color = '#fb7185';

  logTerminal(`[ExecutorService] Spawning fixed thread pool with 8 worker threads...`, 'info');

  const customers = [
    { name: 'Alice', pref: 'VIP-1' },
    { name: 'Bob', pref: 'VIP-1' },       // COLLISION
    { name: 'Charlie', pref: 'VIP-2' },
    { name: 'David', pref: 'VIP-2' },     // COLLISION
    { name: 'Emma', pref: 'VIP-3' },
    { name: 'Frank', pref: 'REG-1' },
    { name: 'Grace', pref: 'REG-1' },     // COLLISION
    { name: 'Hannah', pref: 'REG-2' },
    { name: 'Ian', pref: 'REG-2' },       // COLLISION
    { name: 'Jack', pref: 'REG-3' },
    { name: 'Karen', pref: 'REG-4' },
    { name: 'Leo', pref: 'VIP-1' },       // TRIPLE COLLISION
    { name: 'Mona', pref: 'REG-5' },
    { name: 'Noah', pref: 'REG-6' },
    { name: 'Olivia', pref: 'REG-7' },
    { name: 'Paul', pref: 'REG-8' }
  ];

  let completedTasks = 0;

  customers.forEach((c, idx) => {
    const threadId = `Thread-${(idx % 8) + 1}`;
    const jitter = Math.floor(Math.random() * 800) + 100;

    setTimeout(() => {
      const seat = seatsData.find(s => s.id === c.pref);

      logTerminal(`[${threadId}] Customer '${c.name}' requesting lock on ${c.pref}...`, 'thread-tag');

      if (seat.booked) {
        conflictsCount++;
        logTerminal(`>> RACE CONFLICT PREVENTED: [${threadId}] Seat ${c.pref} already locked by ${seat.bookedBy}! Request aborted safely.`, 'conflict');
        taskDone();
      } else {
        // Seat free, acquire lock
        seat.locking = true;
        renderSeats();

        setTimeout(() => {
          if (seat.booked) {
            // Another thread grabbed it in microsecond window
            conflictsCount++;
            seat.locking = false;
            logTerminal(`>> RACE CONFLICT PREVENTED: [${threadId}] Contention collision on ${c.pref}!`, 'conflict');
          } else {
            seat.locking = false;
            seat.booked = true;
            seat.bookedBy = c.name;
            logTerminal(`>> [BOOKED] [${threadId}] ${c.name} successfully reserved ${c.pref}!`, 'success');
          }
          renderSeats();
          taskDone();
        }, 300);
      }
    }, jitter);
  });

  function taskDone() {
    completedTasks++;
    if (completedTasks === customers.length) {
      setTimeout(() => {
        isSimulating = false;
        threadEngineStatus.textContent = 'Engine Idle (Simulation Finished)';
        threadEngineStatus.style.background = 'rgba(16, 185, 129, 0.15)';
        threadEngineStatus.style.color = '#34d399';
        logTerminal(`[ExecutorService] All 16 threads safely synchronized. Zero double-bookings!`, 'success');
        updateTelemetry();
      }, 500);
    }
  }
});

document.getElementById('resetSeatsBtn').addEventListener('click', () => {
  seatsData.forEach(s => { s.booked = false; s.bookedBy = null; s.locking = false; });
  conflictsCount = 0;
  terminalBox.innerHTML = '<div class="terminal-line info">[System] Hall reset to default initial state.</div>';
  renderSeats();
});

// Initial Render
renderSeats();
