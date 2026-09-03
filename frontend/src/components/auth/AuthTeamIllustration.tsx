import React from "react";

export const AuthTeamIllustration: React.FC<{ className?: string }> = ({ className = "" }) => {
  return (
    <div className={`w-full max-w-[480px] mx-auto flex items-center justify-center ${className}`}>
      <svg
        viewBox="0 0 520 400"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="w-full h-auto drop-shadow-xs select-none"
      >
        {/* Background Office Window */}
        <rect x="35" y="60" width="160" height="155" rx="3" stroke="#CBD5E1" strokeWidth="2.5" fill="#F8FAFC" />
        <line x1="35" y1="135" x2="195" y2="135" stroke="#CBD5E1" strokeWidth="2" />
        <line x1="115" y1="60" x2="115" y2="215" stroke="#CBD5E1" strokeWidth="2" />

        {/* Background Office Wall Artwork */}
        <rect x="385" y="95" width="85" height="60" rx="3" stroke="#CBD5E1" strokeWidth="2" fill="#F8FAFC" />
        <circle cx="415" cy="125" r="14" fill="#E2E8F0" />
        <polygon points="435,140 455,140 445,115" fill="#CBD5E1" />

        {/* Background Credenza / Cabinet */}
        <rect x="25" y="275" width="470" height="90" rx="4" fill="#F1F5F9" stroke="#E2E8F0" strokeWidth="1.5" />
        <line x1="25" y1="310" x2="495" y2="310" stroke="#E2E8F0" strokeWidth="1.5" />
        <line x1="25" y1="345" x2="495" y2="345" stroke="#E2E8F0" strokeWidth="1.5" />
        {/* Cabinet knobs */}
        <circle cx="240" cy="292" r="2.5" fill="#94A3B8" />
        <circle cx="280" cy="292" r="2.5" fill="#94A3B8" />
        <circle cx="240" cy="327" r="2.5" fill="#94A3B8" />
        <circle cx="280" cy="327" r="2.5" fill="#94A3B8" />

        {/* Plant on lower left */}
        <path d="M 45 355 C 38 310, 22 280, 18 240 C 26 270, 36 295, 48 355 Z" fill="#E2E8F0" />
        <path d="M 48 355 C 48 300, 35 260, 32 220 C 44 255, 52 285, 54 355 Z" fill="#CBD5E1" />
        <path d="M 52 355 C 60 290, 72 265, 85 240 C 72 270, 62 300, 56 355 Z" fill="#94A3B8" />
        <rect x="38" y="340" width="26" height="30" rx="3" fill="#E2E8F0" stroke="#CBD5E1" strokeWidth="1.5" />

        {/* Books on the credenza */}
        <rect x="420" y="245" width="7" height="30" rx="1.5" fill="#CBD5E1" />
        <rect x="429" y="240" width="8" height="35" rx="1.5" fill="#94A3B8" />
        <rect x="439" y="248" width="6" height="27" rx="1.5" fill="#E2E8F0" />
        <rect x="447" y="252" width="7" height="23" rx="1.5" fill="#CBD5E1" />

        {/* Floor Line */}
        <line x1="10" y1="365" x2="510" y2="365" stroke="#CBD5E1" strokeWidth="2.5" strokeLinecap="round" />

        {/* =========================================================
            PERSON 1 (LEFT - SEATED ENGINEER)
           ========================================================= */}
        {/* Office Chair Back & Legs */}
        <path d="M 45 260 L 60 355" stroke="#334155" strokeWidth="3" strokeLinecap="round" />
        <path d="M 75 285 L 90 355" stroke="#334155" strokeWidth="3" strokeLinecap="round" />
        <line x1="45" y1="260" x2="75" y2="285" stroke="#334155" strokeWidth="4" strokeLinecap="round" />
        <path d="M 40 260 L 30 200" stroke="#334155" strokeWidth="4" strokeLinecap="round" />

        {/* Legs & Shoes */}
        {/* Left Leg */}
        <path d="M 75 280 L 110 280 L 115 345" fill="none" stroke="#E2E8F0" strokeWidth="16" strokeLinecap="round" strokeLinejoin="round" />
        <path d="M 115 345 L 140 355" fill="none" stroke="#2D3748" strokeWidth="10" strokeLinecap="round" />
        {/* Right Leg */}
        <path d="M 85 280 L 125 280 L 138 345" fill="none" stroke="#CBD5E1" strokeWidth="16" strokeLinecap="round" strokeLinejoin="round" />
        <path d="M 138 345 L 165 355" fill="none" stroke="#2D3748" strokeWidth="10" strokeLinecap="round" />

        {/* Seated Body (Dark Green/Teal Shirt) */}
        <path d="M 50 275 C 50 240, 58 220, 78 215 C 98 220, 108 245, 110 280 Z" fill="#2C5E57" />

        {/* Seated Arms typing on laptop */}
        <path d="M 75 225 L 98 255 L 135 255" fill="none" stroke="#2C5E57" strokeWidth="13" strokeLinecap="round" strokeLinejoin="round" />
        {/* Hand */}
        <circle cx="140" cy="254" r="5" fill="#E8B298" />

        {/* Seated Head & Hair */}
        <circle cx="86" cy="180" r="14" fill="#E8B298" />
        {/* Hair */}
        <path d="M 75 180 C 75 166, 92 163, 98 170 C 99 175, 96 179, 93 182 C 90 180, 80 180, 75 180 Z" fill="#1E293B" />
        {/* Facial profile */}
        <circle cx="94" cy="178" r="1.5" fill="#334155" />
        {/* Nose profile */}
        <path d="M 97 179 L 101 183 L 97 185" fill="none" stroke="#D3987E" strokeWidth="1.5" strokeLinecap="round" />

        {/* =========================================================
            DESK & LAPTOP
           ========================================================= */}
        {/* Modern Yellow/Gold Desk Legs */}
        <path d="M 175 270 L 155 355" stroke="#E5A11E" strokeWidth="3.5" strokeLinecap="round" />
        <path d="M 215 270 L 225 355" stroke="#E5A11E" strokeWidth="3.5" strokeLinecap="round" />
        <path d="M 155 355 L 225 355" stroke="#E5A11E" strokeWidth="2.5" strokeLinecap="round" />

        {/* Desk Surface */}
        <rect x="140" y="263" width="125" height="6" rx="2" fill="#334155" />

        {/* Laptop (Open, screen facing left person) */}
        <path d="M 148 263 L 180 263" stroke="#94A3B8" strokeWidth="3" strokeLinecap="round" />
        <path d="M 180 263 L 195 215" stroke="#475569" strokeWidth="4" strokeLinecap="round" />
        {/* Glow / Screen */}
        <path d="M 178 261 L 192 217" stroke="#E2E8F0" strokeWidth="2" strokeLinecap="round" />

        {/* =========================================================
            PERSON 2 (MIDDLE - LEANING COLLEAGUE)
           ========================================================= */}
        {/* Legs with Yellow/Ochre Pants */}
        <path d="M 245 250 L 248 355" fill="none" stroke="#E5A11E" strokeWidth="18" strokeLinecap="round" />
        <path d="M 268 250 L 278 355" fill="none" stroke="#C9880F" strokeWidth="18" strokeLinecap="round" />
        {/* Blue Shoes */}
        <path d="M 245 355 L 230 365" fill="none" stroke="#1E293B" strokeWidth="9" strokeLinecap="round" />
        <path d="M 278 355 L 298 365" fill="none" stroke="#1E293B" strokeWidth="9" strokeLinecap="round" />

        {/* Torso (Light argyle patterned sweater) */}
        <path d="M 225 250 C 220 215, 230 185, 260 178 C 285 185, 290 215, 280 255 Z" fill="#DDE5ED" />
        {/* Argyle cross hatches on sweater */}
        <path d="M 235 210 L 255 235 L 275 210" fill="none" stroke="#B0C2D4" strokeWidth="2" />
        <path d="M 235 225 L 255 200 L 275 225" fill="none" stroke="#B0C2D4" strokeWidth="2" />
        <line x1="255" y1="195" x2="255" y2="245" stroke="#B0C2D4" strokeWidth="1.5" strokeDasharray="3 3" />

        {/* Arm leaning & pointing toward laptop */}
        <path d="M 235 195 L 205 235 L 180 248" fill="none" stroke="#DDE5ED" strokeWidth="12" strokeLinecap="round" strokeLinejoin="round" />
        <circle cx="176" cy="249" r="4.5" fill="#E8B298" />

        {/* Right arm slightly back */}
        <path d="M 275 198 L 285 240" fill="none" stroke="#C3D2E0" strokeWidth="10" strokeLinecap="round" />

        {/* Head, Hair & Face */}
        <circle cx="252" cy="155" r="13" fill="#E8B298" />
        {/* Hair */}
        <path d="M 242 153 C 242 142, 255 140, 263 146 C 265 152, 260 156, 256 156 C 250 154, 245 154, 242 153 Z" fill="#1E293B" />
        {/* Eye */}
        <circle cx="248" cy="155" r="1.5" fill="#334155" />
        {/* Smile */}
        <path d="M 245 162 Q 248 165 252 163" fill="none" stroke="#D3987E" strokeWidth="1.5" strokeLinecap="round" />

        {/* =========================================================
            PERSON 3 (RIGHT - STANDING WOMAN)
           ========================================================= */}
        {/* Dark Teal Wide-Leg Utility Trousers */}
        <path d="M 335 240 L 328 340" fill="none" stroke="#2C5E57" strokeWidth="20" strokeLinecap="round" />
        <path d="M 358 240 L 366 340" fill="none" stroke="#234E48" strokeWidth="20" strokeLinecap="round" />
        {/* Cargo pocket outline */}
        <rect x="345" y="275" width="22" height="24" rx="2" fill="#234E48" stroke="#3A756D" strokeWidth="1.5" />

        {/* Reddish / Burgundy Ankle Boots */}
        <path d="M 326 340 L 318 358" fill="none" stroke="#99334D" strokeWidth="12" strokeLinecap="round" />
        <path d="M 368 340 L 378 358" fill="none" stroke="#80263D" strokeWidth="12" strokeLinecap="round" />

        {/* Upper Body (Matching Dark Teal Blouse) */}
        <path d="M 325 240 C 322 195, 332 170, 355 165 C 375 170, 382 195, 378 240 Z" fill="#2C5E57" />

        {/* Left Arm gesturing towards laptop */}
        <path d="M 332 175 L 298 200 L 275 208" fill="none" stroke="#2C5E57" strokeWidth="10" strokeLinecap="round" strokeLinejoin="round" />
        {/* Hand gesturing */}
        <path d="M 275 208 L 263 205" fill="none" stroke="#9C5A3E" strokeWidth="4" strokeLinecap="round" />

        {/* Right Arm hanging relaxed */}
        <path d="M 372 175 L 380 220" fill="none" stroke="#234E48" strokeWidth="9" strokeLinecap="round" />

        {/* Head, Hair Topknot & Warm Complexion */}
        <circle cx="350" cy="140" r="13" fill="#9C5A3E" />
        {/* Hair Bun / Topknot */}
        <circle cx="362" cy="123" r="8" fill="#1E293B" />
        <path d="M 342 138 C 342 125, 356 124, 362 130 C 365 137, 358 140, 352 140 Z" fill="#1E293B" />
        {/* Eye */}
        <circle cx="344" cy="138" r="1.5" fill="#334155" />
        {/* Smile */}
        <path d="M 341 145 Q 344 148 348 146" fill="none" stroke="#7A3D24" strokeWidth="1.5" strokeLinecap="round" />
      </svg>
    </div>
  );
};

export default AuthTeamIllustration;
