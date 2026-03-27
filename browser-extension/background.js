const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 5000;
const DOMAIN_COOLDOWN_MS = 5 * 60 * 1000;

let port = null;
let isConnecting = false;
let reconnectAttempts = 0;
const lastReactedDomains = {};

function getDomain(url) {
  try { return new URL(url).hostname; } catch { return null; }
}

function getSearchQuery(url) {
  if (!url) return null;
    try {
      const urlObj = new URL(url);
      return urlObj.searchParams.get('q')      // Google
          || urlObj.searchParams.get('query')  // хз еще какие-то
          || urlObj.searchParams.get('text')   // Яндекс
          || null;
    } catch { return null; }
}

function isOnDomainCooldown(url) {
  const domain = getDomain(url);
  if (!domain) return false;
  const last = lastReactedDomains[domain];
  if (!last) return false;
  return (Date.now() - last) < DOMAIN_COOLDOWN_MS;
}

function markDomainReacted(url) {
  const domain = getDomain(url);
  if (domain) lastReactedDomains[domain] = Date.now();
}

function getSiteCategory(url) {
  if (!url) return 'unknown';
  const u = url.toLowerCase();

  if (u.includes('youtube.com')) return 'youtube';
  if (u.includes('steamcommunity.com') || u.includes('store.steampowered.com/')) return 'steam browser';
  if (u.includes('twitter.com') || u.includes('x.com')) return 'x.com';
  if (u.includes('tumblr.com')) return 'tumblr';
  if (u.includes('drive.google.com')) return 'google disc';
  if (u.includes('mail.google.com')) return 'gmail';
  if (u.includes('vk.com')) return 'vkontakte';
  if (u.includes('fragrantica.ru') || u.includes('fragrantica.com')) return 'fragrantica';
  if (u.includes('pinterest.ru') || u.includes('pinterest.com')) return 'pinterest';
  if (u.includes('music.yandex.ru')) return 'yandex music';
  if (u.includes('https://ficbook.net/authors/0195b284-abd9-71f1-a6ae-e4857e3d9563')) return 'ficbook';
  if (u.includes('https://ru.wikipedia.org/wiki/%D0%91%D0%BE%D0%B3')) return 'god search';
  if (u.includes('https://ru.wikipedia.org/wiki/%D0%9B%D1%8E%D1%86%D0%B8%D1%84%D0%B5%D1%80')) return 'satan search';
  if (u.includes('https://ru.wikipedia.org/wiki/%D0%90%D0%B4%D0%B0%D0%BC')) return 'adam';
  if (u.includes('https://ru.wikipedia.org/wiki/%D0%9B%D0%B8%D0%BB%D0%B8%D1%82')) return 'lilith';


  return 'unknown';
}

function scheduleReconnect() {
  if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
    reconnectAttempts++;
    setTimeout(connectToNativeApp, RECONNECT_DELAY);
  }
}

function handleNativeMessage(message) {
  console.log("Получено от Амона:", message);
}

function handleDisconnect() {
  console.error("Отключено:", chrome.runtime.lastError?.message);
  port = null;
  isConnecting = false;
  scheduleReconnect();
}

function connectToNativeApp() {
  if (port || isConnecting) return;
  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) return;

  isConnecting = true;
  console.log(`Попытка подключения #${reconnectAttempts + 1}...`);

  try {
    port = chrome.runtime.connectNative('com.lars.amon.browser');
    port.onMessage.addListener(handleNativeMessage);
    port.onDisconnect.addListener(handleDisconnect);
    console.log("Амон подключён к браузеру!");
    reconnectAttempts = 0;
    isConnecting = false;
  } catch (error) {
    console.error("Ошибка подключения:", error);
    port = null;
    isConnecting = false;
    scheduleReconnect();
  }
}

function sendToAmon(data) {
  if (port) {
    try {
      port.postMessage(data);
    } catch (error) {
      console.error("Ошибка отправки:", error);
      port = null;
      scheduleReconnect();
    }
  } else {
    console.warn("Нет подключения, сообщение не отправлено");
    connectToNativeApp();
  }
}

chrome.tabs.onActivated.addListener((activeInfo) => {
  chrome.tabs.get(activeInfo.tabId, (tab) => {
    if (chrome.runtime.lastError || !tab?.url) return;
    if (tab.url.startsWith('chrome://') || tab.url.startsWith('about:')) return;

    const category = getSiteCategory(tab.url);
    if (category === 'unknown') return;

    if (isOnDomainCooldown(tab.url)) {
      console.log("Кулдаун домена, пропускаем:", getDomain(tab.url));
      return;
    }

    markDomainReacted(tab.url);
    sendToAmon({
      type: "TAB_ACTIVATED",
      url: tab.url,
      title: tab.title || "",
      category: category,
      searchQuery: null,
      timestamp: Date.now()
    });
  });
});

chrome.tabs.onUpdated.addListener((tabId, changeInfo, tab) => {
  if (changeInfo.status !== 'complete') return;
  if (!tab?.url) return;
  if (tab.url.startsWith('chrome://') || tab.url.startsWith('about:')) return;

  const searchQuery = getSearchQuery(tab.url);
  const category = getSiteCategory(tab.url);

  // поисковые запросы — всегда отправляем
  if (searchQuery) {
    sendToAmon({
      type: "TAB_ACTIVATED",
      url: tab.url,
      title: tab.title || "",
      category: category,
      searchQuery: searchQuery,
      timestamp: Date.now()
    });
    return;
  }

  // обычные сайты — только если вкладка активна в данный момент
  if (category === 'unknown') return;

  chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
    if (!tabs[0] || tabs[0].id !== tabId) return; // вкладка не активна — пропускаем

    if (isOnDomainCooldown(tab.url)) {
      console.log("Кулдаун домена, пропускаем:", getDomain(tab.url));
      return;
    }

    markDomainReacted(tab.url);
    sendToAmon({
      type: "TAB_ACTIVATED",
      url: tab.url,
      title: tab.title || "",
      category: category,
      searchQuery: null,
      timestamp: Date.now()
    });
  });
});

// ИНИЦИАЛИЗАЦИЯ

chrome.runtime.onInstalled.addListener(connectToNativeApp);
chrome.runtime.onStartup.addListener(connectToNativeApp);
connectToNativeApp();