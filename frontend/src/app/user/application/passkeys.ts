import { inject, Injectable } from '@angular/core';
import { Api, apiMessage } from '../../shared/infrastructure/api';

export interface Passkey {
  id: string;
  label: string;
}

@Injectable({ providedIn: 'root' })
export class Passkeys {
  private readonly api = inject(Api);
  private readonly path = '/session/passkeys';

  supported(): boolean {
    return globalThis.isSecureContext === true &&
      typeof globalThis.PublicKeyCredential?.parseCreationOptionsFromJSON === 'function' &&
      typeof globalThis.PublicKeyCredential?.parseRequestOptionsFromJSON === 'function';
  }

  async signIn(): Promise<void> {
    const options = await this.api.mutate<PublicKeyCredentialRequestOptionsJSON>(
      'POST', this.path + '/login/options');
    const publicKey = PublicKeyCredential.parseRequestOptionsFromJSON(options);
    const credential = await navigator.credentials.get({ publicKey });
    await this.api.mutate('POST', this.path + '/login', this.encode(credential));
  }

  async add(password: string, label: string): Promise<void> {
    const options = await this.api.mutate<PublicKeyCredentialCreationOptionsJSON>(
      'POST', this.path + '/register/options', { password });
    const publicKey = PublicKeyCredential.parseCreationOptionsFromJSON(options);
    const credential = await navigator.credentials.create({ publicKey });
    await this.api.mutate('POST', this.path + '/register', { label, credential: this.encode(credential) });
  }

  list(): Promise<Passkey[]> {
    return this.api.get<Passkey[]>(this.path);
  }

  remove(id: string): Promise<void> {
    return this.api.mutate('DELETE', this.path + '/' + encodeURIComponent(id));
  }

  private encode(credential: Credential | null): RegistrationResponseJSON | AuthenticationResponseJSON {
    if (!(credential instanceof PublicKeyCredential)) throw new DOMException('', 'NotAllowedError');
    return credential.toJSON();
  }
}

export function passkeyMessage(error: unknown): string {
  if (error instanceof DOMException) {
    if (error.name === 'NotAllowedError' || error.name === 'AbortError') {
      return 'La solicitud se canceló o el dispositivo no encontró una passkey disponible.';
    }
    if (error.name === 'InvalidStateError') return 'Esta passkey ya está registrada en la cuenta.';
    if (error.name === 'SecurityError') return 'Abre WriteSphere desde su dirección HTTPS o desde localhost.';
  }
  return apiMessage(error);
}
