// PWA Service Worker Registration
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js').catch(console.error);
  });
}

// Local Storage Keys
const DB_KEYS = {
  VIDEOS: 'vyro_videos_v1',
  USER_PROFILE: 'vyro_profile_v1',
  NOTIFICATIONS: 'vyro_notifications_v1',
  MESSAGES: 'vyro_messages_v1',
  LIKED_VIDEOS: 'vyro_liked_v1'
};

// Initial Seed Videos
const INITIAL_VIDEOS = [
  {
    id: 1,
    creator: 'Elena Rostova',
    username: '@elena_cinematic',
    avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
    caption: 'Cyberpunk neon night drives through Shibuya 🌃⚡️',
    tags: '#tokyo #cinematic #vyro',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
    likes: 42300,
    comments: [
      { user: 'Koji_dev', text: 'The color grading is immaculate 🔥' },
      { user: 'Sarah_M', text: 'Which lens was this shot on?' }
    ],
    shares: 1240
  },
  {
    id: 2,
    creator: 'Marcus Vance',
    username: '@marcus_creates',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    caption: 'Golden hour drone shots across the canyon ridge 🦅🏔️',
    tags: '#drone #nature #filmmaker',
    videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4',
    likes: 89100,
    comments: [
      { user: 'AeroVisuals', text: 'Incredible speed control!' }
    ],
    shares: 5320
  }
];

function getStoredVideos() {
  const stored = localStorage.getItem(DB_KEYS.VIDEOS);
  if (!stored) {
    localStorage.setItem(DB_KEYS.VIDEOS, JSON.stringify(INITIAL_VIDEOS));
    return INITIAL_VIDEOS;
  }
  return JSON.parse(stored);
}

function getLikedVideos() {
  const stored = localStorage.getItem(DB_KEYS.LIKED_VIDEOS);
  return stored ? JSON.parse(stored) : [];
}

let currentVideoId = null;

function renderFeed() {
  const container = document.getElementById('video-feed-container');
  const videos = getStoredVideos();
  const liked = getLikedVideos();

  container.innerHTML = videos.map((v) => {
    const isLiked = liked.includes(v.id);
    return `
      <div class="video-card" data-id="${v.id}">
        <video class="video-element" loop playsinline src="${v.videoUrl}" onclick="togglePlay(this)"></video>
        <div class="video-overlay">
          <div class="video-info">
            <div class="creator-tag">
              <img src="${v.avatar}" style="width: 28px; height: 28px; border-radius: 50%; object-fit: cover;">
              <span>${v.creator}</span>
              <span class="follow-chip" onclick="toggleFollow(this)">Follow</span>
            </div>
            <div class="video-caption">${v.caption}</div>
            <div class="hashtags">${v.tags}</div>
          </div>
          <div class="action-sidebar">
            <button class="action-btn ${isLiked ? 'liked' : ''}" onclick="toggleLike(this, ${v.id})">
              <div class="action-icon">❤️</div>
              <span class="action-count">${v.likes}</span>
            </button>
            <button class="action-btn" onclick="openComments(${v.id})">
              <div class="action-icon">💬</div>
              <span class="action-count">${v.comments.length}</span>
            </button>
            <button class="action-btn" onclick="shareVideo(${v.id})">
              <div class="action-icon">↗️</div>
              <span class="action-count">${v.shares}</span>
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');

  setupAutoPlayObserver();
}

function setupAutoPlayObserver() {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      const video = entry.target.querySelector('video');
      if (video) {
        if (entry.isIntersecting) {
          video.play().catch(() => {});
        } else {
          video.pause();
        }
      }
    });
  }, { threshold: 0.65 });

  document.querySelectorAll('.video-card').forEach((card) => observer.observe(card));
}

function togglePlay(video) {
  if (video.paused) {
    video.play();
  } else {
    video.pause();
  }
}

function toggleLike(btn, videoId) {
  let liked = getLikedVideos();
  const isLiked = liked.includes(videoId);

  if (isLiked) {
    liked = liked.filter(id => id !== videoId);
    btn.classList.remove('liked');
  } else {
    liked.push(videoId);
    btn.classList.add('liked');
  }
  localStorage.setItem(DB_KEYS.LIKED_VIDEOS, JSON.stringify(liked));
}

function toggleFollow(btn) {
  if (btn.innerText === 'Follow') {
    btn.innerText = 'Following';
    btn.style.background = '#6366F1';
  } else {
    btn.innerText = 'Follow';
    btn.style.background = 'rgba(255,255,255,0.2)';
  }
}

function navigateTo(screenId, event) {
  document.querySelectorAll('.screen').forEach((s) => s.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach((n) => n.classList.remove('active'));
  
  const targetScreen = document.getElementById(`screen-${screenId}`);
  if (targetScreen) targetScreen.classList.add('active');
  
  if (event && event.currentTarget) {
    event.currentTarget.classList.add('active');
  }

  // Auto pause videos when navigating away
  if (screenId !== 'home') {
    document.querySelectorAll('video').forEach(v => v.pause());
  }
}

function openComments(videoId) {
  currentVideoId = videoId;
  const videos = getStoredVideos();
  const video = videos.find(v => v.id === videoId);
  if (!video) return;

  const commentsList = document.getElementById('modal-comments-list');
  commentsList.innerHTML = video.comments.map(c => `
    <div class="comment-row">
      <strong style="color: #818CF8;">${c.user}:</strong>
      <span>${c.text}</span>
    </div>
  `).join('');

  document.getElementById('comments-modal').classList.add('open');
}

function closeComments() {
  document.getElementById('comments-modal').classList.remove('open');
}

function postComment() {
  const input = document.getElementById('comment-input-field');
  const text = input.value.trim();
  if (!text || !currentVideoId) return;

  const videos = getStoredVideos();
  const video = videos.find(v => v.id === currentVideoId);
  if (video) {
    video.comments.push({ user: 'You', text: text });
    localStorage.setItem(DB_KEYS.VIDEOS, JSON.stringify(videos));
    openComments(currentVideoId);
    input.value = '';
  }
}

function handleUpload() {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'video/*';
  input.onchange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const url = URL.createObjectURL(file);
      const videos = getStoredVideos();
      videos.unshift({
        id: Date.now(),
        creator: 'You',
        username: '@mycreator',
        avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80',
        caption: `New creation: ${file.name} 🚀`,
        tags: '#vyro #creator #shorts',
        videoUrl: url,
        likes: 0,
        comments: [],
        shares: 0
      });
      localStorage.setItem(DB_KEYS.VIDEOS, JSON.stringify(videos));
      renderFeed();
      navigateTo('home');
    }
  };
  input.click();
}

function shareVideo(videoId) {
  if (navigator.share) {
    navigator.share({
      title: 'VYRO Video',
      text: 'Check out this video on VYRO!',
      url: window.location.href
    }).catch(() => {});
  } else {
    navigator.clipboard.writeText(window.location.href);
    alert('Link copied to clipboard!');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  renderFeed();
});
