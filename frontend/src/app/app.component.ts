import { Component, OnDestroy, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, interval } from 'rxjs';
import { WeatherData } from './models/weather-data';
import { WeatherApiService } from './services/weather-api.service';

type CityOption = {
  value: string;
  label: string;
};

type LoadWeatherOptions = {
  notifyOnNewData: boolean;
};

@Component({
  selector: 'app-root',
  imports: [FormsModule, DecimalPipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  readonly cities: CityOption[] = [
    { value: 'Dnipro', label: 'Дніпро' },
    { value: 'Kyiv', label: 'Київ' },
    { value: 'Sumy', label: 'Суми' },
    { value: 'Odesa', label: 'Одеса' }
  ];

  selectedCity = this.cities[0].value;
  weather: WeatherData | null = null;
  statusMessage = 'Перевіряю зʼєднання...';
  errorMessage = '';
  isLoading = false;
  toastVisible = false;
  toastTitle = '🌤️ Погода оновлена!';
  toastBody = '';

  private readonly refreshIntervalMs = 10 * 60 * 1000;
  private readonly toastDurationMs = 8_000;
  private readonly storageKeyPrefix = 'lastWeatherTimestamp:';

  private refreshSub: Subscription | null = null;
  private toastTimeoutId: number | null = null;

  constructor(private readonly weatherApi: WeatherApiService) {}

  ngOnInit(): void {
    this.checkStatus();
    this.loadWeather({ notifyOnNewData: false });
    this.refreshSub = interval(this.refreshIntervalMs).subscribe(() =>
      this.loadWeather({ notifyOnNewData: true })
    );
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
    this.clearToastTimeout();
  }

  onCityChange(): void {
    this.hideToast();
    this.loadWeather({ notifyOnNewData: false });
  }

  manualRefresh(): void {
    this.loadWeather({ notifyOnNewData: true });
  }

  getWeatherEmoji(iconCode: string | undefined): string {
    const map: Record<string, string> = {
      '01': '☀️',
      '02': '⛅',
      '03': '☁️',
      '04': '☁️',
      '09': '🌧️',
      '10': '🌦️',
      '11': '⛈️',
      '13': '❄️',
      '50': '🌫️'
    };
    const key = iconCode ? iconCode.substring(0, 2) : '01';
    return map[key] || '🌤️';
  }

  formatTime(timestamp: number | undefined): string {
    if (!timestamp) {
      return '--';
    }

    return new Date(timestamp).toLocaleTimeString('uk-UA', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  requestNotifications(): void {
    if (!this.isBrowserNotificationSupported()) {
      window.alert('Твій браузер не підтримує сповіщення 😢');
      return;
    }

    window.Notification.requestPermission().then((permission) => {
      if (permission === 'granted') {
        window.alert('✅ Сповіщення увімкнено! Тепер ти побачиш оновлення погоди.');
      } else {
        window.alert('ℹ️ Дозвіл не надано. Toast-повідомлення на сторінці все одно працюватимуть.');
      }
    });
  }

  hideToast(): void {
    this.toastVisible = false;
    this.clearToastTimeout();
  }

  get notificationButtonLabel(): string {
    if (!this.isBrowserNotificationSupported()) {
      return '🔔 Сповіщення недоступні';
    }

    return window.Notification.permission === 'granted'
      ? '🔔 Сповіщення увімкнено'
      : '🔔 Увімкнути сповіщення';
  }

  private loadWeather(options: LoadWeatherOptions): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.weatherApi.getWeather(this.selectedCity).subscribe({
      next: (data) => {
        this.weather = data;

        if (data?.city) {
          this.handleFreshData(data, options.notifyOnNewData);
        }

        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Не вдалося отримати погоду. Перевір, чи backend працює на порту 8090.';
        this.isLoading = false;
      }
    });
  }

  private checkStatus(): void {
    this.weatherApi.getStatus().subscribe({
      next: (status) => {
        this.statusMessage = status.message;
      },
      error: () => {
        this.statusMessage = 'Backend недоступний';
      }
    });
  }

  private handleFreshData(data: WeatherData, notifyOnNewData: boolean): void {
    const storageKey = this.getStorageKey(data.city || this.selectedCity);
    const previousTimestamp = window.localStorage.getItem(storageKey);
    const currentTimestamp = String(data.lastUpdated ?? '');

    if (notifyOnNewData && previousTimestamp && previousTimestamp !== currentTimestamp) {
      this.showNotification(data);
    }

    window.localStorage.setItem(storageKey, currentTimestamp);
  }

  private showNotification(data: WeatherData): void {
    const title = '🌤️ Погода оновлена!';
    const body = `${data.city}: ${Math.round(data.temperature)}°C, ${data.description}`;

    if (this.isBrowserNotificationSupported() && window.Notification.permission === 'granted') {
      new window.Notification(title, {
        body,
        icon: `https://openweathermap.org/img/wn/${data.icon || '01d'}@2x.png`
      });
    }

    this.showToast(data);
  }

  private showToast(data: WeatherData): void {
    this.toastTitle = '🌤️ Погода оновлена!';
    this.toastBody = `🌡️ ${Math.round(data.temperature)}°C • ${data.description}\n💧 ${data.humidity}% • 💨 ${data.windSpeed.toFixed(1)} м/с`;
    this.toastVisible = true;

    this.clearToastTimeout();
    this.toastTimeoutId = window.setTimeout(() => {
      this.toastVisible = false;
      this.toastTimeoutId = null;
    }, this.toastDurationMs);
  }

  private clearToastTimeout(): void {
    if (this.toastTimeoutId !== null) {
      window.clearTimeout(this.toastTimeoutId);
      this.toastTimeoutId = null;
    }
  }

  private isBrowserNotificationSupported(): boolean {
    return typeof window !== 'undefined' && 'Notification' in window;
  }

  private getStorageKey(city: string): string {
    return `${this.storageKeyPrefix}${city}`;
  }
}
