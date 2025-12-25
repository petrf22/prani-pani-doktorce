import { Component, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { UserService } from '../services/user-service';

@Component({
  selector: 'app-verify-token',
  imports: [FormsModule, NzFormModule, NzInputModule, NzInputModule],
  templateUrl: './verify-token.component.html',
  styleUrls: ['./verify-token.component.css']
})
export class VerifyTokenComponent {
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);
  private userService = inject(UserService);

  constructor() {
    // Access route parameters
    this.activatedRoute.params.subscribe((params) => {
      const emailToken = params['emailToken'];

      console.log('VerifyTokenComponent :: activatedRoute :: emailToken:', emailToken);

      this.userService.verifyToken(emailToken).subscribe({
        next: (response) => {
          console.log('VerifyTokenComponent :: Verify token successfully:', response);
          // this.verifyInfo = 'E-mail byl úspěšně ověřen.';

          this.router.navigate(['/prani'], { replaceUrl: true });
        },
        error: (error) => {
          console.error('VerifyTokenComponent :: Error verifying token:', error);
          // this.verifyInfo = 'Chyba při ověřování tokenu z e-mailu.';
        }
      });
    });
  }
}
